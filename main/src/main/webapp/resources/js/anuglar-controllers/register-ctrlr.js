var registerCtrlr = function($scope, $http, Upload){
	
	var today = new Date();
        
        $scope.selectedMemberType = false;
	$scope.medicalUser        = false;
	$scope.recreationalUser   = false;
	
	$scope.user = {'identifications':{}, 'marketing':{ 'subscribe': true}, memberType:'', approveStatus:'0'};
	$scope.reco = {year:new Date().getFullYear()};
	$scope.dob = {};
	$scope.today = today.getFullYear() + "-" + (today.getMonth() + 1) + "-" + today.getDate();
	$scope.recoExpiryErrMsg = "Recommendation Expiry date should " +
			"be a valid future date.";
	
	$scope.referralUserValid = "";
	
	$scope.hearAboutOptions = ['WeedMaps', 'Google', 'Instagram', 'Yelp', 'Facebook', 'Leafly', 'Friends & Family', 'La Weekly', 'Other'];
	
	$scope.register = function(){
		
//		if(!$scope.medicalUser && !$scope.recreationalUser){
//			alert("Invalid user selection. Please refresh the page and try again");
//			return false;
//		}
		
		var proceed = true;
		$scope.pageLevelAlert = "";
		$('.recofile-group, .idfile-group, .dobWrapper, .recoWrapper').removeClass('has-error');		

		
		if($scope.invalidDob()){
			proceed = false;
			$('.dobWrapper').addClass('has-error');
		}
		
		if($scope.recoFile && $scope.invalidReco()){
			proceed = false;
			
			$('.recoWrapper').addClass('has-error');
		}
		
		if($scope.user.marketing && 
				$scope.user.marketing.referrerUsernameEmail &&
				$scope.referralUserValid === "N"){
			proceed = false;
		}
		
		if($scope.registerForm.$valid && 
				//(($scope.medicalUser && $scope.recoFile) || $scope.recreationalUser) &&
				$scope.idFile && 
				proceed){
			
//			$scope.user.memberType = "medical";
//			if($scope.recreationalUser) $scope.user.memberType = "recreational";
			
			$http.post(_lbUrls.register, $scope.user)
			.then(function(response){
				var resp = response.data;	
				if(resp.success){
					
					//Remove files from localStorage
					localStorage.removeItem('recoFile');
					localStorage.removeItem('idFile');
					localStorage.removeItem('stateMariCardFile');
					
					if(resp.message === 'activated'){
						location.href = "/account-activated";
					}
					else{
						location.href = "/pending-registration";
					}
				}
				else{
					$scope.pageLevelAlert = resp.message; 
				}
			},
			
			function(){
				$scope.pageLevelAlert = "There was some error creating your account. " + 
					"Please try later. If problem persists, please call the customer care.";
			});	
		}
		
		else{
			
//			if($scope.medicalUser && !$scope.recoFile){
//				$('.recofile-group').addClass('has-error');
//			}
			
			if(!$scope.idFile){
				$('.idfile-group').addClass('has-error');
			}
			
			if(!$scope.registerForm.$valid){
				$scope.pageLevelError = 'Please correct the highlighted sections'; 
			}
			
			$('html,body').scrollTop($('.has-error').offset().top-120);
		}
	};
	
	
	$scope.validateUsername = function(){
		
		$scope.referralUserValid = "";
		
		if($scope.user && 
				$scope.user.marketing && 
				$scope.user.marketing.referrerUsernameEmail){
			
			$http.post(_lbUrls.register + '/validateuser', $scope.user.marketing.referrerUsernameEmail)
			.then(function(response){
				var resp = response.data;	
				if(resp && resp.success){
					$scope.referralUserValid = "Y";
				}
				else{
					$scope.referralUserValid = "N";
				}
			});
		}
	};
	
	
	$scope.removeFile = function(type){
		
		if(type == 'id' && $scope.idFile){			
			$http.post('/files/delete/id/' + $scope.idFile._id);
			$scope.idFile = null;
			localStorage.removeItem('idFile');
		} else if(type == 'reco' && $scope.recoFile){
			$http.post('/files/delete/id/' + $scope.recoFile._id);
			$scope.recoFile = null;
			localStorage.removeItem('recoFile');
		} else if(type == 'stateMariCardFile' && $scope.stateMariCardFileRef){
			$http.post('/files/delete/id/' + $scope.stateMariCardFileRef._id);
			$scope.stateMariCardFileRef = null;
			localStorage.removeItem('stateMariCardFile');
		}
	};
	
	$scope.invalidFname = function(){		
		return $scope.registerForm.firstname.$invalid && 
			!$scope.registerForm.firstname.$pristine;
	};
	
	$scope.invalidLname = function(){		
		return $scope.registerForm.lastname.$invalid && 
			!$scope.registerForm.lastname.$pristine;
	};
	
	$scope.invalidEmail = function(){		
		return $scope.registerForm.email.$invalid && 
			!$scope.registerForm.email.$pristine;
	};
	
	$scope.invalidUname = function(){		
		return $scope.registerForm.username.$invalid &&  
			!$scope.registerForm.username.$pristine;
	};
	
	$scope.invalidPassword = function(){		
		return $scope.registerForm.password.$invalid && 
			!$scope.registerForm.password.$pristine;
	};
	
	$scope.invalidCPassword = function(){		
		return !$scope.registerForm.confirmpassword.$pristine && 
			($scope.user.password != $scope.confirmPassword);
	};
	
	$scope.invalidPhone = function(){		
		return $scope.registerForm.phone.$invalid && 
			!$scope.registerForm.phone.$pristine;
	};
	
	$scope.invalidDob = function(){	
		
		if($scope.registerForm.dobDay.$valid && 
				$scope.registerForm.dobMonth.$valid && 
				$scope.registerForm.dobYear.$valid){
			
			var now = new Date(),
			currYear = now.getFullYear(),
			year21Before = currYear - 21;
			
			//Set years back by 21
			now.setFullYear(year21Before);
			
			$scope.user.dob = new Date($scope.dob.year, $scope.dob.month-1, $scope.dob.day);
			
			//if user dob is greater than year21Before, then user is below 21 
			if($scope.user.dob.getTime() > now.getTime()){
				return true;
			}
		}
		
		return ($scope.registerForm.dobDay.$invalid && !$scope.registerForm.dobDay.$pristine) || 
			($scope.registerForm.dobMonth.$invalid && !$scope.registerForm.dobMonth.$pristine) || 
			($scope.registerForm.dobYear.$invalid && !$scope.registerForm.dobYear.$pristine);
	};
	
	$scope.invalidReco = function(){
		
		if($scope.reco.year && 
				$scope.reco.month &&  
				$scope.reco.day){
		
			var now = new Date();
			$scope.user.identifications.recoExpiry = new Date($scope.reco.year, $scope.reco.month-1, $scope.reco.day);
			
			if($scope.user.identifications.recoExpiry.getTime() < now.getTime()){
				$scope.recoExpiryErrMsg = "Recommendation Expiry date should be a valid future date.";
				return true;
			}
		}
		
		$scope.recoExpiryErrMsg = "Recommendation Expiry date is required if " +
		"you have a recommendation letter";
		
		return (!$scope.reco.year || !$scope.reco.month || !$scope.reco.day);
	};
       
       
    $scope.chooseMemberType = function (option) {      
        if (option === 'recreational') {
            $scope.user.memberType      = 'recreational';
            $scope.selectedMemberType   = true;
        } else if (option === 'medical') {
            $scope.user.memberType      = 'medical';
            $scope.selectedMemberType   = true;
        }
    };
        
    $scope.$watch('idfileobj', function () {
        new $scope.upload($scope.idfileobj, 'id');
    });

	
    $scope.$watch('recofileobj', function () {
        new $scope.upload($scope.recofileobj,'reco');
    });
    
    $scope.$watch('stateMariCardFile', function () {
        new $scope.upload($scope.stateMariCardFile,'stateMariCard');
    });
    
    
    $scope.upload = function (file, type) {
      
        if (file) {
	        var fileInfo = {progress : false, element : null};
	        
	        Upload.upload({
	            url: '/files/upload',
	            fields : {path : '/user/'+$scope.today + "/", ctrl : 'controlled'},
	        	file: file,
	        	fileInfo : fileInfo
	            
	        }).then(
	
	    	        function (resp) {
                            
                                // Success
		    	                                     
			           if(resp.config.fileInfo)
			           resp.config.fileInfo.element.closest('.row').remove();
			
			           if(resp.data.results){
			        	   var img = resp.data.results[0];
			        	   
			        	   if(img){
				        	   if(type && type=='id'){
				        		   $scope.user.identifications.idCard = img.location;
				        		   $scope.idFile = img;
				        		   localStorage.setItem('idFile', JSON.stringify(img));
				        	   } else if(type && type=='reco'){
				        		   $scope.user.identifications.recomendation = img.location;
				        		   $scope.recoFile = img;
				        		   localStorage.setItem('recoFile', JSON.stringify(img));
				        	   } else if(type && type=='stateMariCard'){
				        		   $scope.user.identifications.stateMarijuanaCard = img.location;
				        		   $scope.stateMariCardFileRef = img;
                                                                                                                      
				        		   localStorage.setItem('stateMariCardFile', JSON.stringify(img));
				        	   }
			        	   }
			           }
			            
			        },
	
	
	
	    	        function (resp) {//Error
			            if(resp.config.fileInfo)
			            	resp.config.fileInfo.element.parent().html('<div class="progress-bar progress-bar-danger"style="width: 100%">Error Uploading File</div>');
			            
			            console.log('Error uploading the file');
			        },
	
	
	    	        function (evt) { //Progress
			        	if(!evt.config.fileInfo.progress){
				        	var f = evt.config.fileInfo;
				        	
				        	var container = '.progress-wrapper ';
				        	if(type) container = '.' + type + '-progress-wrapper';
				        	
			        		f.progress = true;
			        		f.element = $('<div />').attr({'class': 'progress-bar progress-bar-success', 'role':'progressbar','style':'width:0%'});
			        		
			        		$('<div class="row"><div class="col-xs-4 file-name-holder">' + 
			                	evt.config.file.name + 
			                	'</div><div class="col-xs-8"><div class="progress"></div></div>')
			                .appendTo(container)
			                .find('.progress')
			                .append(f.element);
			        	}
	
			            if(evt.config.fileInfo){
			            	var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
			            	evt.config.fileInfo.element.css('width', progressPercentage+'%').html(progressPercentage+'%');
			            }
			        }
			 );
      }
    };
    
    $scope.init = function(){
    	
    	try {
    		
        	if(localStorage.getItem('recoFile')){
        		$scope.recoFile = JSON.parse(localStorage.getItem('recoFile')); 
      		   	$scope.user.identifications.recomendation = $scope.recoFile.location;
     	   	}
        	
        	if(localStorage.getItem('idFile')){
        		$scope.idFile = JSON.parse(localStorage.getItem('idFile'));
        		$scope.user.identifications.idCard = $scope.idFile.location;
        	}
                
                if(localStorage.getItem('stateMariCardFile')){
                        if($scope.user.memberType === 'medical'){
                            $scope.stateMariCardFileRef = JSON.parse(localStorage.getItem('stateMariCardFile'));
                            $scope.user.identifications.stateMarijuanaCard = $scope.stateMariCardFileRef.location;
                	}else{
                            localStorage.removeItem('stateMariCardFile');
                        }
                }
                
    	}catch(e){}
    };
    
    $scope.init();
};