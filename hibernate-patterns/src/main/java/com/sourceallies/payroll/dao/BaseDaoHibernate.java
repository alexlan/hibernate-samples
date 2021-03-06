package com.sourceallies.payroll.dao;

import com.sourceallies.payroll.domain.Identifiable;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;

import java.util.List;

public class BaseDaoHibernate<B extends Identifiable> implements BaseDao<B> {
    private SessionFactory sessionFactory;
    private Class<B> queryClass;
    private String defaultOrder = "id";
    private boolean defaultOrderAsc = true;

    protected Session getSession() {
        return getSessionFactory().getCurrentSession();
    }

    public B getNewInstance() {
        try {
			return getQueryClass().newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Error creating new instance of : " + getQueryClass().getName(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Error creating new instance of : " + getQueryClass().getName(), e);
		}
    }

    protected Criteria getBaseCriteria() {
		return getSession().createCriteria(getQueryClass());
	}

    @SuppressWarnings("unchecked")
	public List<B> getAll() {
		return addDefaultOrder(getBaseCriteria()).list();
	}

    @SuppressWarnings("unchecked")
	public B getById(Long id) {
		return (B) getSession().get(queryClass, id);
	}

    public void save(B obj) {
		getSession().save(obj);
	}

    public void update(B obj) {
		getSession().update(obj);
	}

    public void saveOrUpdate(B obj) {
		getSession().saveOrUpdate(obj);
	}

    public void delete(B obj) {
		getSession().delete(obj);
	}

    public int getCountAll() {
		return getCount(getBaseCriteria());
	}

    public int getCountByExample(B example) {
		return getCount(getExampleCriteria(example));
	}

    protected int getCount(Criteria criteria) {
		criteria.setProjection(Projections.rowCount());

		Integer result = (Integer) criteria.uniqueResult();
		if (result == null) {
			return 0;
		} else {
			return result;
		}
	}

    protected Criteria getExampleCriteria(B example) {
		Example hibExample = Example.create(example);

		return getBaseCriteria().add(hibExample);
	}

    public List<B> getPageOfDataAll(PaginationInfo pageInfo) {
		return getPageOfData(getBaseCriteria(), pageInfo);
	}

    public List<B> getPageOfDataByExample(B example, PaginationInfo pageInfo) {
		return getPageOfData(getExampleCriteria(example), pageInfo);
	}

    @SuppressWarnings("unchecked")
	protected List<B> getPageOfData(Criteria criteria, PaginationInfo pageInfo) {
		return addPaginationInfo(criteria, pageInfo).list();
	}

    protected Criteria addPaginationInfo(Criteria criteria, PaginationInfo pageInfo) {
		String column = pageInfo.getSortColumn();
		if (column == null) {
			addDefaultOrder(criteria);
		} else {
			boolean sortAsc = pageInfo.isSortAsc();
			addOrder(criteria, column, sortAsc);
		}

		if (pageInfo.getFirstRow() != -1) {
			criteria.setFirstResult(pageInfo.getFirstRow());
		}

		if (pageInfo.getMaxResults() != -1) {
			criteria.setMaxResults(pageInfo.getMaxResults());
		}

		return criteria;
	}

    protected Criteria addOrder(Criteria criteria, String column, boolean sortAsc) {
		if (sortAsc) {
			criteria.addOrder(Order.asc(column));
		} else {
			criteria.addOrder(Order.desc(column));
		}

		return criteria;
	}

    protected Criteria addDefaultOrder(Criteria criteria) {
		return addOrder(criteria, getDefaultOrder(), isDefaultOrderAsc());
	}

    @SuppressWarnings("unchecked")
    public List<B> getByExample(B example) {
        Criteria crit = getExampleCriteria(example);
        addDefaultOrder(crit);

        return crit.list();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Class<B> getQueryClass() {
        return queryClass;
    }

    public void setQueryClass(Class<B> queryClass) {
        this.queryClass = queryClass;
    }

    public String getDefaultOrder() {
        return defaultOrder;
    }

    public void setDefaultOrder(String defaultOrder) {
        this.defaultOrder = defaultOrder;
    }

    public boolean isDefaultOrderAsc() {
        return defaultOrderAsc;
    }

    public void setDefaultOrderAsc(boolean defaultOrderAsc) {
        this.defaultOrderAsc = defaultOrderAsc;
    }
}
