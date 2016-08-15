//Create a random number for version control and avoid caching
var versionCtrl = '?v=' + Math.random();

var usrApp = angular.module(
	'userApps', [
         'ngRoute',
         'ngSanitize',
         'ui.bootstrap',
         'uploadModule',
         'ngFileUpload'
     ]
);


/**
 * All the controller functions are implemented here. 
 * Scroll to the bottom to see it being attached to the App.
 */ 
var 
defaultCtrlr = function($scope, $http, $filter, $sanitize, $interval){
	
	$scope.pageLevelError = '';
	$scope.pg = {};
	$scope.pg.currentPage = 1;
	$scope.users = [];
	$scope.userStatus = 'all';
	
	$scope.sortType = '_id';
	$scope.sortReverse = true;
	$scope.userSearch = '';
	
	$scope.getUsers = function(){
		
		$http.get('/admin/users/json/', {
			params : { 
				'p' : $scope.pg.currentPage,
				'q' : $scope.userSearch,
				's' : $scope.userStatus,
				'o' : ($scope.sortReverse?'-':'') + $scope.sortType
			}

		}).success(function(data){
			if(data.success){
				$scope.users = data.respData;
				$scope.pg = data.pg
			}
			else if(data.message && data.message!=''){
				$scope.pageLevelError = data.message;
			}
			else{

				$scope.pageLevelError = "There was some error getting users list."
					+" Please contact G";
			}

		}).error(function(){
			$scope.pageLevelError = "There was some error getting users list."
					+" Please contact G";
		});	
		
	};
	
	/*Reload the users page every 5 minutes*/
	$scope.reloadUsers = function(){
		if($scope.pg.currentPage == 1) $scope.getUsers();
	};
	
	$scope.pageChanged = function() {
		$scope.getUsers();
	};
	
	$scope.getUsers();
	$interval($scope.reloadUsers, 5 * 60 * 1000);
},

usrCtrlrs = function($scope, $http, $filter, $routeParams, $location, mode, $sanitize, uploadService, $rootScope){
	
	$scope.params = $routeParams;
	$scope.errorMsg = '';
	$scope.p = {};
	$scope.roles = ["customer","admin"];
	$scope.statuses = [true,false];
	$scope.mode = mode; //Mode will be 'new' while adding a user, and 'edit', if editing a user.
	$scope.userId = $scope.params.userId;
	
	/**UPLOAD SPECIFIC FUNCTIONS*/
	$scope.cdnPath = uploadService.cdnPath;
	
	$scope.afterSelect = function(){
		if(uploadService.selectedFiles){
			if(!$scope.u.identifications) $scope.u.identifications = {};
			$scope.u.identifications.idCard = uploadService.selectedFiles[0].location;
			$scope.user.$setDirty();
		}
	};
	
	$scope.selectFile = function(){
		uploadService.config.cb = $scope.afterSelect;	
		uploadService.config.fields = {path : '/user/', ctrl : 'controlled'};		
		uploadService.showGallery($rootScope);
	};

	$scope.afterRecoFileSelect = function(){
		if(uploadService.selectedFiles){
			if(!$scope.u.identifications) $scope.u.identifications = {};
			$scope.u.identifications.recomendation = uploadService.selectedFiles[0].location;
			$scope.user.$setDirty();
		}
	};
	
	$scope.selectRecoFile = function(){
		uploadService.config.cb = $scope.afterRecoFileSelect;		
		uploadService.showGallery($rootScope);
	};
	/**UPLOAD SPECIFIC FN ENDS*/

	
	var getCouponsAssigned = function(createCoupon){
		
		$scope.promos = [];		
		$http.get('/admin/users/json/getcpromos/' + $scope.u._id)
		.then(function(response){
			if(response && response.data){
				
				var resp = response.data;				
				if(resp.success) {
					$scope.promos = resp.results;
				}
			}				
		});		
	};
	
	$scope.getUserDetails = function($event){
		
		$http.get('/admin/users/json/' + $scope.userId)
		.success(function(data){
			$scope.u = data;			
			$('#user-editor').show();
			
		    if($scope.u.identifications && $scope.u.identifications.recoExpiry)
		    		$scope.recoExpiry = new Date($scope.u.identifications.recoExpiry);
		    
		    if($scope.u.dob)
	    		$scope.dob = new Date($scope.u.dob);
		    
		    getCouponsAssigned();
		    
		}).error(function(){
			$scope.errorMsg = 'There was some error getting the product info. Please try later';
			$('#user-editor').show();
		});		
	};	
	
	$scope.closeUserDetails = function(){
		$scope.initalize();	
		$('#user-editor').hide();
	};
	
	
	$scope.showPasswordForm = function(){
		$('.user-details-holder').slideUp();
		$('.user-password-update').slideDown();
	};
	
	
	$scope.showUserForm = function(){
		$('.user-details-holder').slideDown();
		$('.user-password-update').slideUp();
	};
	
	
	$scope.recoExpiry = '';
	$scope.updateRecoExpiry = function(){
		if(!$scope.u.identifications) $scope.u.identifications = {};
		
		$scope.u.identifications.recoExpiry = $scope.recoExpiry ? $scope.recoExpiry.getTime() : '';  
	};
	
	$scope.dob = '';
	$scope.updateDob = function(){		
		$scope.u.dob = $scope.dob ? $scope.dob.getTime() : '';  
	};
	
	
	$scope.savePassword = function(){
		
		$http.post('/admin/users/json/savep', {'_id' : $scope.u._id, 'password' : $scope.password1 })
		.success(function(resp){
			if(resp.success) {
				
				$scope.password1 = '';
				$scope.password2 = '';
				
				$scope.u.password = resp.message;
				_lbFns.pSuccess('Password Updated');
				
				$scope.showUserForm();
			}
			else{
				alert(resp.message);
			}
				
		})
		.error(function(){
			$scope.errorMsg  = "There was some error saving the password. "
					+"Please contact G.";
		});	
	};
	
	
	
	
	$scope.activateNemail = function(createCoupon){
		
		var param = ""; 
		if(createCoupon) param = "?c=coupon";
		
		$http.get('/admin/users/json/activate-n-email/' + $scope.u._id + param)
		.then(function(response){
			if(response && response.data){
				
				var resp = response.data;				
				if(resp.success) {
					$scope.u.active = true;
					
					var msg = "";					
					if(resp.message && resp.message.indexOf('CP')==0){
						msg = resp.message.split(":")[1];
					}
					else if(createCoupon && resp.message){
						msg = "Coupon " + resp.message 
							+ " was created and included in the email."
					}
					
					_lbFns.pSuccess('User activated and email send. ' + msg);
				}
				else{
					alert(resp.message);
				}
			}
			else{
				$scope.errorMsg  = "There was some error activating the account. "
						+"Please contact G.";
			}
				
		},
		
		function(){
			$scope.errorMsg  = "There was some error activating the account. "
					+"Please contact G.";
		});
		
	};	
	
	
	$scope.saveUserGeneric = function(cb){	
		
		/**
		 * When mode is new, we need to generate userId in the first call to
		 * the server. Once Id is generated, we switch back to edit mode by
		 * calling the edit URL 
		 **/
		if($scope.mode == 'new'){
			
			$http.post('/admin/users/json/create', $scope.u)
			.success(function(resp){
				if(resp.success){
					_lbFns.pSuccess('User Created');
					$location.path('/details/' + resp.message);
				}
				
				else{
					$scope.errorMsg  = resp.message;
				}
			})
			.error(function(){
				$scope.errorMsg  = "There was some error creating the user. "
						+"Please contact G";
			});
			
		}
		else{
			
			$http.post('/admin/users/json/save', $scope.u)
			.success(function(resp){
				if(cb)
					cb(resp);
			})
			.error(function(){
				$scope.errorMsg  = "There was some error saving the user details. "
						+"Please contact G.";
			});			
		}
	};
	
	
	$scope.saveUser = function(){
		
		if($scope.user.$dirty){			
			$scope.saveUserGeneric(function(resp){
				if(resp.success){
					$scope.user.$setPristine();
					_lbFns.pSuccess('User updated');				
				}
				else{
					$scope.errorMsg = resp.message;
				}
			});	
		}
	};	
	
	$scope.initalize = function(){
		$scope.errorMsg = '';
		$scope.u = {};	
    	$location.path('');	
	};
	
	
	if($scope.mode=='edit' && $scope.userId)
		$scope.getUserDetails();
	
	else {
		$scope.u = {};	
		$scope.u._id = 0;
		$scope.u.password = '123456';
		$scope.role = 'customer';
		$('#user-editor').show();
	}

};

usrApp.config(['$routeProvider',
    function($routeProvider) {
      $routeProvider.
	    when('/details/:userId', {
	         templateUrl: '/resources/ng-templates/admin/user-details.html'+versionCtrl,
	         controller: 'userControllers',
	         resolve : {
	        	 mode:function () {
	        		 return 'edit';
	        	 }
	         }
	    }).
	    
	    when('/adduser/', {
	         templateUrl: '/resources/ng-templates/admin/user-details.html'+versionCtrl,
	         controller: 'userControllers',
	         resolve : {
	        	 mode:function () {
	        		 return 'new';
	        	 }
	         }
	    }).
	    
	    when('', {
	         templateUrl: '/resources/ng-templates/empty.html',
	         controller: 'defaultRouteCtrl'
	    });
}])



//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('defaultRouteCtrl', defaultCtrlr)
.controller('userControllers', usrCtrlrs)

//This is defined in angular-upload-service.js. Common fn for uploads
.controller('uploadCtrlr', uploadCtrlr);

