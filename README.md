<img src="https://github.com/wielski/KinopoiskApi/blob/master/logo.png?raw=true">

Kinopoisk API PHP Sdk
==========
Based on mobile Kinopoisk application.
Docs: <a href="http://kinopoisk.cf/docs">http://kinopoisk.cf/docs</a>

<h4>Using</h4>

    $Kinopoisk = new wielski\KinopoiskApi\Kinopoisk;
    $Kinopoisk->call('getFilm', [
      'kinopoiskId' = 1
    ]);
