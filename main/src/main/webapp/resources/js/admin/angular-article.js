//Create a random number for version control and avoid caching
var versionCtrl = '?v=' + Math.random();

var artApp = angular.module(
	'articlesApps', [
         'ngSanitize',
         'ui.bootstrap'
     ]
);


/**
 * All the controller functions are implemented here. 
 * Scroll to the bottom to see it being attached to the App.
 */ 
var 
defaultCtrlr = function($scope, $http, $filter, $sanitize, $templateRequest, $compile){
	
	$scope.pageLevelError = '';
	$scope.pg = {};
	$scope.pg.currentPage = 1;
	$scope.articles = [];
	$scope.at = {};
	$scope.statuses = ['draft','publish','private'];
	
	$scope.sortType = '_id';
	$scope.sortReverse = true;
	$scope.articleSearch = '';
	
	$scope.getArticles = function(){
		
		$http.get('/admin/articles/json/', {
			params : { 
				'p' : $scope.pg.currentPage,
				'q' : $scope.articleSearch,
				'o' : ($scope.sortReverse?'-':'') + $scope.sortType
			}

		}).success(function(data){
			if(data.success){
				$scope.articles = data.respData;
				$scope.pg = data.pg
			}
			else if(data.message && data.message!=''){
				$scope.pageLevelError = data.message;
			}
			else{

				$scope.pageLevelError = "There was some error getting the articles."
					+" Please contact G";
			}

		}).error(function(){
			$scope.pageLevelError = "There was some error getting the articles."
					+" Please contact G";
		});	
		
	};
	
	$scope.pageChanged = function() {
		$scope.getArticles();
	};
	
	$scope.getArticles();	
	

	
	/**
	 * On article title blur event or permlink change event, if the permlink is empty
	 * we will create URL based on the article title
	 **/
	$scope.createPermaLink = function(){		
		if($scope.at.title && $scope.at.title != '' 
				&& (!$scope.at.permalink || $scope.at.permalink == '')){
			
			$scope.at.permalink = $scope.at.title.toLowerCase().replace(/\s+/g,"-");
		}
	};
	
	
	$scope.articlePopUp = function(){
		
		//Show popup		
		$templateRequest("/resources/ng-templates/admin/article-modal.html")
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('body').addClass('noscroll').append(template);
		      $compile(template)($scope);
		 });
		
	};
	
	
	$scope.closeModal = function(){
		angular.element('.pop-gallery-overlay').remove();
		angular.element('body').removeClass('noscroll');
	};
	
	
	
	/**
	 * Add/Edit Article
	 **/
	
	$scope.newArticle = function(){	
		$scope.at = {};
	    $scope.mode = 'new';	
		$scope.articlePopUp();
	};

	
	$scope.editArticle = function(){
	    $scope.mode = 'edit';
		$scope.at = this.a;
		
		$scope.articlePopUp();
	};
	
	
	$scope.createArticle = function(){
		
		var url = "save";
		if($scope.mode == 'new') url = 'create';
		
		$http.post('/admin/articles/json/' + url, $scope.at)
		.then(function(response){
			var resp = response.data;
			if(resp.success){
				
				if($scope.mode == 'new') {
					_lbFns.pSuccess('Article created - ' + resp.message);
				}
				else {
					_lbFns.pSuccess('Article updated');
				}

				//Close the modal
				$scope.closeModal();
				
				//Refresh the article list
				$scope.getArticles();
			}	
			else{
				$scope.errorMsg  = resp.message;
			}
		},
		
		function(){
			$scope.errorMsg  = "There was some error saving the article. "
					+"Please contact G.";
		});	
	};
    
};

artApp
//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('defaultRouteCtrl', defaultCtrlr);
