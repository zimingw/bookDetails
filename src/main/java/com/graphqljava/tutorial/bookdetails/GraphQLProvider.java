package com.graphqljava.tutorial.bookdetails;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.TypeResolutionEnvironment;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentationOptions;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.dataloader.DataLoaderRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class GraphQLProvider {

    private GraphQL graphQL;

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @PostConstruct
    public void init() throws IOException {
        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        DataLoaderDispatcherInstrumentationOptions options = DataLoaderDispatcherInstrumentationOptions.newOptions()
                .includeStatistics(true);
        DataLoaderDispatcherInstrumentation dispatcherInstrumentation = new
                DataLoaderDispatcherInstrumentation(options);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema)
                .instrumentation(dispatcherInstrumentation)
                .build();

    }

    @Autowired
    GraphQLDataFetchers graphQLDataFetchers;

    @Autowired
    IsEditableWiringFactory isEditableWiringFactory;

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring(typeRegistry);
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring(TypeDefinitionRegistry typeRegistry) {
        Builder builder = RuntimeWiring.newRuntimeWiring()
                .wiringFactory(isEditableWiringFactory)
                .type(newTypeWiring("Query")
                        .dataFetcher("bookById", graphQLDataFetchers.getBookByIdDataFetcher()))
                .type(newTypeWiring("Book")
                        .dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcher())
                        .dataFetcher("fields", graphQLDataFetchers.getFieldsDataFetcher()))
                .type(newTypeWiring("FieldUnion")
                        .typeResolver(this::getTypeFromIssueExpressObject))
                .type(newTypeWiring("TypedField")
                        .typeResolver(this::getTypeFromIssueExpressObject));

        return builder.build();
    }

    /**
     * Determine graphql type given an issue express object. Use the __typename property
     * if the object is a map, otherwise use simple class name.
     */
    private GraphQLObjectType getTypeFromIssueExpressObject(TypeResolutionEnvironment env) {
        if (Map.class.isAssignableFrom(env.getObject().getClass())) {
            Map<String, Object> result = env.getObject();
            return (GraphQLObjectType)env.getSchema().getType((String)result.get("__typename"));
        }
        return getTypeFromSimpleClassName(env);
    }

    private GraphQLObjectType getTypeFromSimpleClassName(TypeResolutionEnvironment env) {
        String className = env.getObject().getClass().getSimpleName();
        return (GraphQLObjectType)env.getSchema().getType(className);
    }
}