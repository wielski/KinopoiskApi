<?php

namespace wielski\KinopoiskApi;

Class Kinopoisk
{
  private $KP_APIURL = 'https://ext.kinopoisk.ru/ios/3.11.0/';
  private $KP_SECRET = 'a17qbcw1du0aedm';
	private $methods = array(
		'getBestFilms' => 'getBestFilmsList',
		/* Информация фильмов */
		'getGallery' => 'getGallery',
		'getStaff' => 'getStaffList',
		'getFilm' => 'getKPFilmDetailView',
		'getSimilar' => 'getKPFilmsList',
		/* Жанры */
		'getGenres' => 'getKPGenresView',
		'getTopGenre' => 'getKPTopGenre',
		/* Отзывы */
		'getReviews' => 'getKPReviews',
		'getReviewDetail' => 'getKPReviewDetail',
		'getPeopleDetail' => 'getKPPeopleDetailView',
		/* Новости */
		'getNews' => 'getKPNewsView',
		'getNewsDetail' => 'getKPNewsDetail',
		/* Скоро в прокате */
		'getSoonFilms' => 'getKPSoonFilms',
		'getSoonDVD' => 'getKPSoonDVD',
		'getDatesForSoonFilms' => 'getDatesForSoonFilms',
		'getDatesForSoonDVD' => 'getDatesForSoonDVD',
		/* Кинотеатры */
		'getTodayFilms' => 'getKPTodayFilms',
		'getCinemas' => 'getKPCinemas',
		'getCinemaDetail' => 'getKPCinemaDetailView',
		'getSeance' => 'getSeance',
		'getDatesForDetailCinema' => 'getDatesForDetailCinema',
		'getDatesForSeance' => 'getDatesForSeance',
		/* ТОП */
		'getTop' => 'getKPTop',
		/* Поиск */
		'searchGlobal' => 'getKPGlobalSearch',
		'searchFilms' => 'getKPSearchInFilms',
		'searchPeople' => 'getKPSearchInPeople',
		'searchCinemas' => 'getKPSearchInCinemas',
    'searchNavigator' => 'navigator',
    'navigatorFilters' => 'navigatorFilters',
		/* Гео */
		'getCountryList' => 'getKPCountryView',
		'getCityList' => 'getKPAllCitiesView',
		'getPopularIndex' => 'getPopularIndex',
	);

	public function __construct(){
    $this->customFunctions = array(
      'getPopularIndex'
    );

    $this->customOptions = array(
      'getFilm' => function($options){
        if(!isset($options['keyword'])){
          $options['still_limit'] = '9';
        }
        if(!isset($options['keyword'])){
          $options['cityID'] = '1';
        }
        return $options;
      },
      'getSimilar' => function($options){
        if(isset($options['type'])) return $options;
        return array('type' => 'kp_similar_films');
      },
      'searchGlobal' => function($options){
        if(isset($options['keyword'])){
          $options['keyword'] = str_replace('+', ' плюс ', $options['keyword']);
          $options['keyword'] = str_replace(' ', '.', $options['keyword']);
        }
        return $options;
      },
      'searchFilms' => function($options){
        if(isset($options['keyword'])){
          $options['keyword'] = str_replace('+', ' плюс ', $options['keyword']);
          $options['keyword'] = str_replace(' ', '.', $options['keyword']);
        }
        return $options;
      },
      'searchPeople' => function($options){
        if(isset($options['keyword'])){
          $options['keyword'] = str_replace('+', ' плюс ', $options['keyword']);
          $options['keyword'] = str_replace(' ', '.', $options['keyword']);
        }
        return $options;
      },
      'searchCinemas' => function($options){
        if(isset($options['keyword'])){
          $options['keyword'] = str_replace('+', ' плюс ', $options['keyword']);
          $options['keyword'] = str_replace(' ', '.', $options['keyword']);
        }
        return $options;
      },
      'searchNavigator' => function($options){
        if(!isset($options['genre_or'])){
          $options['genre_or'] = '0';
        }
        if(!isset($options['country_or'])){
          $options['country_or'] = '0';
        }
        if(!isset($options['page'])){
          $options['page'] = '1';
        }
        if(!isset($options['order'])){
          $options['order'] = 'rating';
        }

        return $options;
      },
    );
  }

  public function getPopularIndex($options){
    $top = array();
    for($i = 1; $i <= 5; $i++){
      $pageTop = $this->callMethod('getKPTop', [
        'type' => 'kp_item_top_popular_films',
        'page' => $i
        ]);
      $pageTop = json_decode($pageTop, true);

      $top = array_merge($top, $pageTop['data']['items']);
    }

    $place = false;
    $foundFilm = false;

    foreach($top as $i => $film){
      if($options['filmID'] == $film['id']){
        $place = $i + 1;
        $foundFilm = $film;
      }
    }

    if($place){
      $foundFilm['place'] = $place;
      return $foundFilm;
    }
    return 'null';
  }

	public function call($method, $options = array())
	{
		if(empty($this->methods[$method])) return false;

		if(!empty($this->customOptions[$method])){
			$options = array_merge($options, $this->customOptions[$method]($options));
		}
		$options = array_merge($options, ['uuid' => 'b551edb50f87445ba338f307a2e6baee']);

		if (!empty($this->customOptions[$method])) {
			$options = array_merge($options, $this->customOptions[$method]($options));
		}

		foreach($this->methods as $method_name => $method_original) {
			if ($method == $method_name) $method = $method_original;
		}

		if (in_array($method, $this->customFunctions)) {
			$result = $this->{$method}($options);
		}
		else {
			$result = $this->callMethod($method, $options);
			$result = json_decode($result, true);
			$result = $result['data'];
		}

		unset($result['class']);
		if (isset($result['keyword']) && isset($_GET['keyword'])) {
			$result['keyword'] = $_GET['keyword'];
		}

		$result = json_encode($result);
		if ($result == '[key:false]') {
			return false;
		}

		return $result;
	}

	public function callMethod($method, $options)
	{
		$key = $this->key($method, $options);
		$action = $method;
		$query = http_build_query($options);
		if (!empty($query)) $action.= '?' . $query;

		$url = $this->KP_APIURL . $action . '?key=' . $key;
		if (!empty($query)) {
			$url = $this->KP_APIURL . $action . '&key=' . $key;
		}

		$ch = curl_init($url);
    curl_setopt($ch, CURLOPT_USERAGENT, 'Android client (5.0 / api21), ru.kinopoisk/3.11.2 (27) (gzip)');
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 6);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
      'device:android',
      'Android-Api-Version:22',
      'countryID:2',
      'ClientId:'.md5(rand(0, 99999)),
      'clientDate:'.date('H:i m.d.Y'),
      'cityID:2',
      'Image-Scale:3',
      'Cache-Control:max-stale=0',
      'User-Agent:Android client (5.1 / api22), ru.kinopoisk/3.7.0 (45)',
      'Accept-Encoding:gzip',
      'Cookie:user_country=ru'
    ));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

		return $page;
	}

	public function key($method, $options)
	{
		$action = $method;
		$query = http_build_query($options);
		if (!empty($query)) $action.= '?' . $query;
		$key = md5($action . $this->KP_SECRET);
		return $key;
	}

}
