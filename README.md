> :warning: This package is deprecated.
> Please use https://github.com/siqwell/kinopoisk

<img src="https://github.com/wielski/KinopoiskApi/blob/master/logo.png?raw=true">

Kinopoisk API Sdk
==========
Based on mobile Kinopoisk application.
Docs: <a href="http://kinopoisk.cf/docs">http://kinopoisk.cf/docs</a>

<h4>Usage</h4>
<h5>PHP</h5>

SDK presented as class with cUrl request factory and ready for REST calls

```
    $Kinopoisk = new wielski\KinopoiskApi\Kinopoisk;
    $Kinopoisk->call('getFilm', [
      'kinopoiskId' = 1
    ]);
```

<h5>Java</h5>

SDK presended as interceptor for OkHttp client and Retrofit library.
To use it, you have to define your own Retrofit client and intercept there interceptor:

```
KinopoiskRequestInterceptor interceptor = new KinopoiskRequestInterceptor();

OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(new KinopoiskRequestInterceptor())
        .build();
Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build();

```

Also you have to define endpoints for REST calls.
See (Retrofit docs)[https://square.github.io/retrofit/] for examples.
