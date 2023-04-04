package ru.clevertec.ecl.dao.impl;

import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.clevertec.ecl.dao.GiftCertificatesDAO;
import ru.clevertec.ecl.models.GiftCertificate;
import ru.clevertec.ecl.models.criteries.FilterCriteria;
import ru.clevertec.ecl.models.criteries.PaginationCriteria;
import ru.clevertec.ecl.models.criteries.SQLFilter;
import ru.clevertec.ecl.models.criteries.SortCriteria;
import ru.clevertec.ecl.utils.SQLHelper;

import java.util.List;
import java.util.Optional;

import static ru.clevertec.ecl.utils.constants.GiftCertificateParams.CERTIFICATE_ID;
import static ru.clevertec.ecl.utils.constants.GiftCertificatesSQL.*;

@Repository
public class GiftCertificatesDAOImpl implements GiftCertificatesDAO {

    private final JdbcTemplate template;
    private final SessionFactory factory;

    @Autowired
    public GiftCertificatesDAOImpl(SessionFactory factory) {
        this.template = null;
        this.factory = factory;
    }

    @Override
    public List<GiftCertificate> getAllGiftCertificates(FilterCriteria filterCriteria, SortCriteria sortCriteria, PaginationCriteria pagination) {
        StringBuilder sqlWithFilterAndSoring = new StringBuilder("SELECT gc FROM GiftCertificate gc LEFT JOIN FETCH gc.tags");
        SQLFilter sqlFilter = SQLHelper.getSQLFilter(filterCriteria);
        sqlWithFilterAndSoring.append(sqlFilter.getSql());
        sqlWithFilterAndSoring.append(SQLHelper.getSQLOrder(sortCriteria));
        String sql = sqlWithFilterAndSoring.toString();
        Query<GiftCertificate> query = factory.getCurrentSession().createQuery(sql, GiftCertificate.class);
        query.setFirstResult(pagination.getOffset()).setMaxResults(pagination.getLimit());
        sqlFilter.getFilteringFields().forEach(query::setParameter);
        return query.getResultList();
    }

    @Override
    public Optional<GiftCertificate> getGiftCertificateById(long id) {
        Query<GiftCertificate> query = factory.getCurrentSession()
                .createQuery(SELECT_GIFT_CERTIFICATE_WITH_TAGS_BY_ID, GiftCertificate.class)
                .setParameter(CERTIFICATE_ID, id);
        GiftCertificate giftCertificate;
        try {
            giftCertificate = query.getSingleResult();
        } catch (NoResultException e) {
            giftCertificate = null;
        }
        return Optional.ofNullable(giftCertificate);
    }

    @Override
    public GiftCertificate addGiftCertificate(GiftCertificate giftCertificate) {
        factory.getCurrentSession().persist(giftCertificate);
        return giftCertificate;
    }

    @Override
    public GiftCertificate updateGiftCertificate(long id, GiftCertificate certificate) {
        Session session = factory.getCurrentSession();
        session.merge(certificate);
        System.out.println(1);
        session.flush();
        return certificate;
    }

    @Override
    public boolean deleteGiftCertificate(long id) {
        return factory.getCurrentSession()
                .createMutationQuery("DELETE GiftCertificate gc WHERE gc.id = :id")
                .setParameter("id", id)
                .executeUpdate() != 0;
    }
}
