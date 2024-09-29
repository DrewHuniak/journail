package com.hackathon.Journail.BO;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PineconeBoImpl implements PineconeBo {
    private final String USER_ID = "userId";
    private final String DATE = "date";
    private final VectorStore pineconeStore;

    public PineconeBoImpl(VectorStore pineconeStore) {
        this.pineconeStore = pineconeStore;
    }


    @Override
    public List<PineconeEntry> get(String query, String userId) {
        List<Document> results = pineconeStore.similaritySearch(
                SearchRequest.defaults()
                        .withQuery(query)
                        .withFilterExpression(String.format("%s == '%s'", USER_ID, userId)));

        return results.stream().map(this::documentToEntry).collect(Collectors.toList());
    }

    @Override
    public List<PineconeEntry> getByDate(String query, String userId, String date) {
        List<Document> results = pineconeStore.similaritySearch(
                SearchRequest.defaults()
                        .withQuery(query)
                        .withFilterExpression(String.format(
                                "%s == '%s' && %s == '%s'",
                            USER_ID, userId, DATE, date)));

        return results.stream().map(this::documentToEntry).collect(Collectors.toList());
    }

    @Override
    public void save(PineconeEntry newEntry) {
        Document toStore = new Document(newEntry.getContent(), Map.of(
                USER_ID, newEntry.getUserId(),
                DATE, newEntry.getDate()));
        pineconeStore.add(List.of(toStore));
    }

    private PineconeEntry documentToEntry(Document document) {
        Map<String, Object> metadata = document.getMetadata();

        return new PineconeEntry(
                metadata.get(USER_ID).toString(),
                document.getContent(),
                metadata.get(DATE).toString());
    }
}
