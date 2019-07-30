package com.graphqljava.tutorial.bookdetails;

import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IsEditableWiringFactory implements WiringFactory {
    @Autowired
    GraphQLDataFetchers graphQLDataFetchers;

    @Override
    public boolean providesDataFetcher(FieldWiringEnvironment environment) {
        return environment.getFieldDefinition().getName().equals("isEditable");
    }

    @Override
    public DataFetcher getDataFetcher(FieldWiringEnvironment environment) {
        return graphQLDataFetchers.getIsEditableDataFetcher();
    }
}
