package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.WebImageParameters;
import org.springframework.stereotype.Repository;

@Repository
public interface WebImageParametersDao {
    WebImageParameters getById(int id);

    void insert(WebImageParameters parameters);
}
