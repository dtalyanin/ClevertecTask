package ru.clevertec.ecl.services.impl;

import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.ecl.dao.GiftCertificatesRepository;
import ru.clevertec.ecl.dto.GiftCertificateDto;
import ru.clevertec.ecl.dto.UpdateGiftCertificateDto;
import ru.clevertec.ecl.exceptions.EmptyItemException;
import ru.clevertec.ecl.exceptions.ItemExistException;
import ru.clevertec.ecl.exceptions.ItemNotFoundException;
import ru.clevertec.ecl.models.GiftCertificate;
import ru.clevertec.ecl.models.Tag;
import ru.clevertec.ecl.models.codes.ErrorCode;
import ru.clevertec.ecl.models.criteries.FilterCriteria;
import ru.clevertec.ecl.models.responses.ModificationResponse;
import ru.clevertec.ecl.services.GiftCertificatesService;
import ru.clevertec.ecl.services.TagsService;
import ru.clevertec.ecl.utils.mappers.GiftCertificateMapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GiftCertificatesServiceImpl implements GiftCertificatesService {
    private final GiftCertificatesRepository repository;
    private final GiftCertificateMapper mapper;
    private final TagsService tagsService;

    @Autowired
    public GiftCertificatesServiceImpl(GiftCertificatesRepository repository,
                                       GiftCertificateMapper mapper,
                                       TagsService tagsService) {
        this.repository = repository;
        this.mapper = mapper;
        this.tagsService = tagsService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GiftCertificateDto> getAllGiftCertificates(FilterCriteria filter, Pageable pageable) {
        Specification<GiftCertificate> certificateSpecification = Specification.allOf(getSpecificationFromFilter(filter));
        System.out.println(pageable);
        pageable = validateSort(pageable);
        System.out.println(pageable);
        return mapper.convertGiftCertificatesToDtos(repository.findAll(certificateSpecification, pageable).getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public GiftCertificateDto getGiftCertificateById(long id) {
        Optional<GiftCertificate> certificate = repository.findById(id);
        if (certificate.isEmpty()) {
            throw new ItemNotFoundException("Gift certificate with ID " + id + " not found in database",
                    ErrorCode.CERTIFICATE_ID_NOT_FOUND);
        }
        return mapper.convertGiftCertificateToDto(certificate.get());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GiftCertificate> getOptionalGiftCertificateById(long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public ModificationResponse addGiftCertificate(GiftCertificateDto dto) {
        GiftCertificate certificate = mapper.convertDtoToGiftCertificate(dto);
        if (checkGiftCertificateExist(certificate)) {
            throw new ItemExistException("Cannot add: gift certificate with similar name, description, price " +
                    "and duration already exist in database", ErrorCode.CERTIFICATE_EXIST);
        }
        Set<Tag> tags = tagsService.addAllTagsIfNotExist(certificate.getTags());
        certificate.setTags(tags);
        repository.save(certificate);
        return new ModificationResponse(certificate.getId(), "Gift certificate added successfully");
    }

    @Override
    @Transactional
    public ModificationResponse updateGiftCertificate(long id, UpdateGiftCertificateDto dto) {
        checkFieldsForUpdatingExist(dto);
        GiftCertificate certificateWithFieldsToUpdate = mapper.convertUpdateDtoToGiftCertificate(dto);
        Set<Tag> tags = tagsService.addAllTagsIfNotExist(certificateWithFieldsToUpdate.getTags());
        certificateWithFieldsToUpdate.setTags(tags);
        Optional<GiftCertificate> oCertificate = repository.findById(id);
        if (oCertificate.isEmpty()) {
            throw new ItemNotFoundException("Cannot update: gift certificate with ID " + id + " not found",
                    ErrorCode.CERTIFICATE_ID_NOT_FOUND);
        }
        GiftCertificate giftCertificateForUpdate = oCertificate.get();
        mapper.updateGiftCertificateFields(certificateWithFieldsToUpdate, giftCertificateForUpdate);
        if (checkGiftCertificateExist(giftCertificateForUpdate)) {
            throw new ItemExistException("Cannot update: gift certificate with similar name, description, price " +
                    "and duration already exist in database", ErrorCode.CERTIFICATE_EXIST);
        }
        repository.save(giftCertificateForUpdate);
        return new ModificationResponse(id, "Gift certificate updated successfully");

    }

    @Override
    @Transactional
    public ModificationResponse deleteGiftCertificate(long id) {
        int deletedCount = repository.deleteById(id);
        if (deletedCount == 0) {
            throw new ItemNotFoundException("Cannot delete: gift certificate with ID " + id + " not found",
                    ErrorCode.CERTIFICATE_ID_NOT_FOUND);
        }
        return new ModificationResponse(id, "Gift certificate deleted successfully");
    }

    private void checkFieldsForUpdatingExist(UpdateGiftCertificateDto dto) {
        boolean fieldsForUpdatingNotExist = Stream.of(
                        dto.getName(),
                        dto.getDescription(),
                        dto.getTags(),
                        dto.getPrice(),
                        dto.getDuration())
                .allMatch(Objects::isNull);
        if (fieldsForUpdatingNotExist) {
            throw new EmptyItemException("Cannot update: no fields to update",
                    ErrorCode.NO_CERTIFICATE_FIELDS_FOR_UPDATE);
        }
    }

    private boolean checkGiftCertificateExist(GiftCertificate certificate) {
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("id", "createDate", "lastUpdateDate");
        Example<GiftCertificate> existExample = Example.of(certificate, matcher);
        return repository.exists(existExample);
    }

    private List<Specification<GiftCertificate>> getSpecificationFromFilter(FilterCriteria filter) {
        List<Specification<GiftCertificate>> specifications = new ArrayList<>();
        if (filter != null) {
            if (checkFieldIsNotEmpty(filter.getName())) {
                Specification<GiftCertificate> nameSpecification = (root, query, criteriaBuilder)
                        -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%");
                specifications.add(nameSpecification);
            }
            if (checkFieldIsNotEmpty(filter.getDescription())) {
                Specification<GiftCertificate> descriptionSpecification = (root, query, criteriaBuilder) ->
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + filter.getDescription().toLowerCase() + "%");
                specifications.add(descriptionSpecification);
            }
            if (filter.getTags() != null) {
                filter.getTags().stream().filter(this::checkFieldIsNotEmpty).forEach(tag -> {
                    Specification<GiftCertificate> nameSpec = (root, query, criteriaBuilder) -> {
                        Join<GiftCertificate, Tag> tagsJoin = root.join("tags");
                        return criteriaBuilder.equal(tagsJoin.get("name"), tag);
                    };
                    specifications.add(nameSpec);
                });
            }
        }
        return specifications;
    }

    private boolean checkFieldIsNotEmpty(String field) {
        return field != null && !field.isBlank();
    }

    private Pageable validateSort(Pageable pageable) {
        List<Sort.Order> orders = pageable.getSort().get()
                .filter(order -> "name".equals(order.getProperty()) || "createdDate".equals(order.getProperty()))
                .collect(Collectors.toList());
        if (orders.size() != pageable.getSort().stream().count()) {
            Sort sort = Sort.by(orders);
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        } else {
            return pageable;
        }
    }
}
