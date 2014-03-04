/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package net.firejack.platform.core.store;

import net.firejack.platform.core.annotation.Projection;
import net.firejack.platform.core.domain.AbstractDTO;
import net.firejack.platform.core.exception.BusinessFunctionException;
import net.firejack.platform.core.model.AbstractModel;
import net.firejack.platform.core.utils.ClassUtils;
import net.firejack.platform.core.utils.Paging;
import net.firejack.platform.core.utils.SearchQuery;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.transform.Transformers;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.annotation.PostConstruct;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.*;


public abstract class ProjectionStore<E extends AbstractModel, V extends AbstractDTO, ID extends Serializable> extends AbstractStore<E, ID> implements IProjectionStore<E, V> {

    protected Class<V> view;
    protected Map<String, String> aliases;
    protected Map<String, String> fieldPaths;
    protected ProjectionList projection;
    protected Integer index;

    @PostConstruct
    public void init() {
        this.clazz = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.view = (Class<V>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        this.aliases = new HashMap<String, String>();
        this.fieldPaths = new HashMap<String, String>();
        this.projection = Projections.projectionList();
        this.index = 0;

        List<Field> fields = ClassUtils.getFields(view, new ArrayList<Field>());
        for (Field field : fields) {
            findAlias(getClazz(), field);
        }
    }

    @Override
    public List<V> advancedSearchReport(List<List<SearchQuery>> searchQueries, final Paging paging) {
        final LinkedList<Criterion> criterions = new LinkedList<Criterion>();
        final Map<String, String> aliases = new LinkedHashMap<String, String>();
        aliases.putAll(this.aliases);

        replaceFieldPaths(searchQueries);

        parseAdvancedSearchRequest(searchQueries, criterions, aliases, paging);

        return getHibernateTemplate().execute(new HibernateCallback<List<V>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<V> doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = prepareCriteria(session, criterions, aliases, paging, true, true);
                return criteria.list();
            }
        });
    }

    @Transactional
    public Integer advancedSearchCount(List<List<SearchQuery>> searchQueries, Paging paging) {
        LinkedList<Criterion> criterions = new LinkedList<Criterion>();
        Map<String, String> aliases = new LinkedHashMap<String, String>();
        aliases.putAll(this.aliases);

        replaceFieldPaths(searchQueries);

        parseAdvancedSearchRequest(searchQueries, criterions, aliases, paging);

        return searchCount(criterions, aliases, true, true);
    }

    private void replaceFieldPaths(List<List<SearchQuery>> searchQueries) {
        for (List<SearchQuery> searchQueryList : searchQueries) {
            for (SearchQuery searchQuery : searchQueryList) {
                String field = searchQuery.getField();
                String fieldPath = fieldPaths.get(field);
                if (fieldPath != null) {
                    searchQuery.setField(fieldPath);
                }
            }
        }
    }

    protected Criteria prepareCriteria(Session session, LinkedList<Criterion> criterionList, Map<String, String> aliases, Paging paging, boolean isOr, boolean isLeft) {
        aliases.putAll(this.aliases);
        Criteria criteria = super.prepareCriteria(session, criterionList, aliases, paging, isOr, isLeft);

        criteria.setProjection(projection);
        criteria.setResultTransformer(Transformers.aliasToBean(this.view));

        return criteria;
    }

    protected void findAlias(Class<?> clazz, Field field) {
        Projection projection = field.getAnnotation(Projection.class);
        if (projection != null) {
            String path = projection.value();
            fieldPaths.put(field.getName(), path);
            String[] fieldNames = path.split("\\.");
            if (fieldNames.length > 1) {
                String indexedFieldName = null;
                for (int i = 0; i < fieldNames.length; i++) {
                    String fieldName = fieldNames[i];
                    PropertyDescriptor propertyDescriptor = ClassUtils.getPropertyDescriptor(clazz, fieldName);
                    if (propertyDescriptor != null) {
                        Method readMethod = propertyDescriptor.getReadMethod();
                        if (readMethod != null) {
                            Class<?> returnType = readMethod.getReturnType();
                            if (Collection.class.isAssignableFrom(returnType)) {
                                returnType = (Class<?>) ((ParameterizedTypeImpl) readMethod.getGenericReturnType()).getActualTypeArguments()[0];
                            }
                            if (AbstractModel.class.isAssignableFrom(returnType)) {
                                clazz = returnType;

                                String alias = i == 0 ? fieldName : indexedFieldName + "." + fieldName;
                                indexedFieldName = this.aliases.get(alias);
                                if (indexedFieldName == null) {
                                    indexedFieldName = fieldName + this.index++;
                                    this.aliases.put(alias, indexedFieldName);
                                }
                            }
                        } else {
                            throw new BusinessFunctionException("The field '" + fieldName + "' has not read method in class '" + clazz + "'");
                        }
                    } else {
                        throw new BusinessFunctionException("The field '" + fieldName + "' does not exist in class '" + clazz + "'");
                    }
                }
                if (indexedFieldName != null) {
                    String projectionFieldName = indexedFieldName + '.' + fieldNames[fieldNames.length - 1];
                    this.projection.add(Projections.property(projectionFieldName), field.getName());
                }
            } else if (fieldNames.length == 1) {
                this.projection.add(Projections.property(fieldNames[0]), field.getName());
            }
        }
    }
}
