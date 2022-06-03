[![Build Status](https://dev.azure.com/bakdata/public/_apis/build/status/bakdata.kserve-client?repoName=bakdata%2Fkserve-client&branchName=main)](https://dev.azure.com/bakdata/public/_build/latest?definitionId=32&repoName=bakdata%2Fkserve-client&branchName=main)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.bakdata.kserve%3Akserve-client&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=com.bakdata.kserve%3Akserve-client)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.bakdata.kserve%3Akserve-client&metric=coverage)](https://sonarcloud.io/summary/new_code?id=com.bakdata.kserve%3Akserve-client)
[![Maven](https://img.shields.io/maven-central/v/com.bakdata.kserve/kserve-client.svg)](https://search.maven.org/search?q=g:com.bakdata.kserve%20AND%20a:kserve-client&core=gav)


# kserve-client

A Java client for calling KServe inference services which implement one of [the predict v1 or v2 protocols](https://kserve.github.io/website/modelserving/v1beta1/serving_runtime/). 

It let's you easily configure the endpoint of the inference service which should be called. 
The data shape of both the request and response can be modeled using Java classes. 
The library includes a retry mechanism to automatically retry requests to the inference service in case it's scaled to zero upon the first request.

You can find a [blog post on medium](https://medium.com/bakdata/xxx) where the kserve-client is used in the demo application.

## Getting Started

You can add kserve-client via Maven Central.

#### Gradle
```gradle
compile group: 'com.bakdata.kserve', name: 'kserve-client', version: '1.0.1'
```

#### Maven
```xml
<dependency>
    <groupId>com.bakdata.kserve</groupId>
    <artifactId>kserve-client</artifactId>
    <version>1.0.1</version>
</dependency>
```


For other build tools or versions, refer to the [latest version in MvnRepository](https://mvnrepository.com/artifact/com.bakdata.kserve/kserve-client/latest).

### Usage

This usage example is extracted from a [blog post on medium](https://medium.com/bakdata/xxx) where the kserve-client is used. In the inference service, we use an [Argos Translate](https://github.com/argosopentech/argos-translate) model to obtain a translation for an input text.

A KServe inference service supporting the [protocol version v2](https://kserve.github.io/website/modelserving/inference_api) is expected to run on `localhost:8080` with model `argos-translator-en-es` so that the endpoint `localhost:8080/v2/models/argos-translator-en-es/infer` can be used for requests.
This inference service knows how to deal with the fields defined in `TextToTranslate.java` and `Translation.java` for the request input and output data, respectively.

A usage example compatible with protocol version v1 can be constructed analogously using the `KServeClientFactoryV1` class.

\
`TextToTranslate.java`
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TextToTranslate {
    private String textToTranslate;
}
```

\
`Translation.java`
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Translation {
    private String originalText;
    private String translatedText;
}
```

\
`TranslatorResponse.java`
```java
public class TranslatorResponse extends InferenceResponse<Translation> {
}
```

\
`KServeRequester.java`
```java
public class KServeRequester<I, O> {
    private final KServeClient<I> kServeClient;

    public KServeRequester() {
        this.kServeClient = (KServeClient<I>) new KServeClientFactoryV2().getKServeClient(
                "localhost:8080",
                "argos-translator-en-es",
                Duration.ofSeconds(2),
                false
        );
    }

    protected Optional<O> requestInferenceService(final I jsonObject) {
        try {
            return (Optional<O>) this.kServeClient.makeInferenceRequest(
                    jsonObject,
                    TranslatorResponse.class,
                    "");
        } catch (final IOException e) {
            throw new IllegalArgumentException(
                    "Error occurred when sending the inference request or receiving the response", e);
        }
    }
}
```

\
`App.java`
```java
public final class App {
    private static Translation getTranslation(final TextToTranslate input) {
        return new KServeRequester<InferenceRequest<TextToTranslate>, TranslatorResponse>()
                .requestInferenceService(InferenceRequest.<TextToTranslate>builder()
                        .inputs(List.of(
                                RequestInput.<TextToTranslate>builder()
                                        .name("Translation")
                                        .datatype("BYTES")
                                        .shape(List.of(1))
                                        .datatype("BYTES")
                                        .parameters(Parameters.builder()
                                                .contentType("str")
                                                .build())
                                        .data(input)
                                        .build()
                        ))
                        .build())
                .map(InferenceResponse::getOutputs)
                .stream()
                .flatMap(Collection::stream)
                .map(ResponseOutput::getData)
                .findFirst()
                .orElseThrow();
    }

    public static void main(final String[] args) {
        final Translation translation = getTranslation(TextToTranslate.builder().textToTranslate("Hello World").build());
        System.out.println(translation.getTranslatedText());
        // Hola Mundo
    }
}
```

## Development

If you want to contribute to this project, you can simply clone the repository and build it via Gradle.
All dependencies should be included in the Gradle files, there are no external prerequisites.

```bash
> git clone git@github.com:bakdata/kserve-client.git
> cd kserve-client && ./gradlew build
```

Please note, that we have [code styles](https://github.com/bakdata/bakdata-code-styles) for Java.
They are basically the Google style guide, with some small modifications.

## Contributing

We are happy if you want to contribute to this project.
If you find any bugs or have suggestions for improvements, please open an issue.
We are also happy to accept your PRs.
Just open an issue beforehand and let us know what you want to do and why.

## License
This project is licensed under the MIT license.
Have a look at the [LICENSE](https://github.com/bakdata/kserve-client/blob/main/LICENSE) for more details.