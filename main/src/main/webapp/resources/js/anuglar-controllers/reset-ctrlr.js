var resetCtlr = function($scope, $http, $rootScope){
	
	$scope.successNotification = "";
	
	$scope.invalidPassword = function(){		
		return $scope.resetRequest.password.$invalid 
			&& !$scope.resetRequest.password.$pristine;
	};
	
	$scope.invalidCPassword = function(){		
		return !$scope.resetRequest.cpassword.$pristine
			&& ($scope.password != $scope.cpassword);
	};
	
	$scope.requestReset = function(){
		
		if($scope.emailUsername != ''){
			$http.get(_lbUrls.creset, {
				params : { 
					u : $scope.emailUsername
				}
			})
			.then(
					function(response){
						var resp = response.data;
						if(resp.success){
							$scope.successNotification = 
								"We have emailed the password reset link to your registered email address";							
						}
						else{
							$scope.pageLevelAlert = resp.message;
						}
						
					},
					function(){
						$scope.pageLevelAlert = $rootScope.errMsgContactSupport;
					}
				);
		}
		else{
			$scope.pageLevelAlert = "Please provide username or email to reset your password.";
		}
		
	};
	
	
	$scope.changePassword = function(){
		
		if($scope.password != '' && ($scope.password == $scope.cpassword)){
			
			$http.post(_lbUrls.rsavep, {
				username : $('#formusername').val(),
				email : $('#formemail').val(),
				password : $scope.password
			})
			.then(
					function(response){
						var resp = response.data;
						if(resp.success){
							$scope.successNotification = 
								"Password changed successfully";							
						}
						else{
							$scope.pageLevelAlert = resp.message;
						}
						
					},
					function(){
						$scope.pageLevelAlert = $rootScope.errMsgContactSupport;
					}
				);
		}
		else{
			
			if($scope.password == ''){
				$scope.pageLevelAlert = "Please provide valid passwords";
			}
			
			else if($scope.password != $scope.cpassword){
				$scope.pageLevelAlert = "Passwords don't match";
			}
		}
		
	};
};