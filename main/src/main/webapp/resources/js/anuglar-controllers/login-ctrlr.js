var loginCtrlr = function($scope, $http){
	
	$scope.user = {};
	
	$scope.login = function(){
		var req = {
				 method: 'POST',
				 url: _lbUrls.login,
				 headers: {
					 'Content-Type': 'application/x-www-form-urlencoded'
				 },
				 params: {
					'username':$scope.user.username, 
					'password':$scope.user.password,
					'rememberme':true
				}
		};

		$http(req)
		.then(function(resp){
			if(resp.data){
				if(resp.data.pending){
					location.href = resp.data.pending;
				}
				else if(resp.data.success){
					if($('#redirectURL').val()!=''){
						location.href = $('#redirectURL').val();
					}
					else 
						location.href = resp.data.success;
				}
				else if(resp.data.authfailure){
					$scope.pageLevelAlert = resp.data.authfailure;
				}
				else{
					$scope.pageLevelAlert = "There was some error. Please try again later.";
				}

			}
			else{	
				$scope.pageLevelAlert = "There was some error. Please try again later.";
			}

		},
		function(){
			$scope.pageLevelAlert = "There was some error. Please try again later.";
		});	
		
		return false;
	};
};