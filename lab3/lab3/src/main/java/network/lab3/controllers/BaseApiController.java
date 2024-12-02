package network.lab3.controllers;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public abstract class BaseApiController<T, R> {
    protected final OkHttpClient httpClient;

    public BaseApiController() {
        this.httpClient = new OkHttpClient();
    }

    protected abstract Request getRequest();
    protected abstract R parseResponse(ResponseBody responseBody) throws IOException;
    protected abstract T extractResult(R response);

    public CompletableFuture<T> search() {
        CompletableFuture<T> future = new CompletableFuture<>();

        httpClient.newCall(getRequest()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        R parsedResponse = parseResponse(responseBody);
                        future.complete(extractResult(parsedResponse));
                    } else {
                        future.completeExceptionally(new IOException("Empty response body"));
                    }
                } else {
                    future.completeExceptionally(new IOException("Request failed with status code: " + response.code()));
                }
            }
        });

        return future;
    }
}
