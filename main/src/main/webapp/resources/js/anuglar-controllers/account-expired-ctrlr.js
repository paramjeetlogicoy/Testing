var acctExpiredCtrlr = function($scope, $http, Upload){
	
	var today = new Date();
	
	$scope.reco = {year:new Date().getFullYear()};
	$scope.today = today.getFullYear() + "-" + (today.getMonth() + 1) + "-" + today.getDate();
	$scope.user = { 
			identifications : {
				recoExpiry : '',
				recomendation : ''
			}
	};
	
	$scope.submitForm = function(){
		
		var proceed = true;
		$scope.pageLevelAlert = "";
		$('.recofile-group, .recoWrapper').removeClass('has-error');		
		
		if($scope.invalidReco()){
			proceed = false;
			$('.recoWrapper').addClass('has-error');
		}
		
		if($scope.recoUploadForm.$valid && 
				$scope.recoFile && 
				proceed){
			
			$http.post(_lbUrls.recoreupload, $scope.user)
			.then(function(response){
				var resp = response.data;	
				if(resp.success){
					
					//Remove file from localStorage
					localStorage.removeItem('recoFile');					
					location.href = "/pending-verification";
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
			
			if(!$scope.recoFile){
				$('.recofile-group').addClass('has-error');
			}
			
			if(!$scope.recoUploadForm.$valid){
				$scope.pageLevelError = 'Please correct the highlighted sections'; 
			}
			
			$('html,body').scrollTop($('.has-error').offset().top-120);
		}
	};
	
	
	$scope.removeFile = function(type){		
		if(type == 'reco' && $scope.recoFile){
			$http.post('/files/delete/id/' + $scope.recoFile._id);
			$scope.recoFile = null;
			localStorage.removeItem('recoFile');
		}
	};
	
	$scope.invalidReco = function(){
		
		if($scope.recoUploadForm.recoDay.$valid && 
				$scope.recoUploadForm.recoMonth.$valid &&  
				$scope.recoUploadForm.recoYear.$valid){
		
			var now = new Date();
			$scope.user.identifications.recoExpiry = new Date($scope.reco.year, $scope.reco.month-1, $scope.reco.day);
			
			if($scope.user.identifications.recoExpiry.getTime() < now.getTime()){
				return true;
			}
		}
		
		return ($scope.recoUploadForm.recoDay.$invalid && !$scope.recoUploadForm.recoDay.$pristine) || 
			($scope.recoUploadForm.recoMonth.$invalid && !$scope.recoUploadForm.recoMonth.$pristine) ||  
			($scope.recoUploadForm.recoYear.$invalid && !$scope.recoUploadForm.recoYear.$pristine);
	};
	
	
    $scope.$watch('recofileobj', function () {
        new $scope.upload($scope.recofileobj,'reco');
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
	
	    	        function (resp) { //Success
		    	        
			            if(resp.config.fileInfo)
			            	resp.config.fileInfo.element.closest('.row').remove();
			
			           if(resp.data.results){
			        	   var img = resp.data.results[0];
			        	   
			        	   if(img){
				        	   if(type && type=='reco'){
				        		   $scope.user.identifications.recomendation = img.location;
				        		   $scope.recoFile = img;
				        		   localStorage.setItem('recoFile', JSON.stringify(img));
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
        	$scope.user._id = $("#formUserId").val()
    	}catch(e){}
    	
    	try {    		
        	if(localStorage.getItem('recoFile')){
        		$scope.recoFile = JSON.parse(localStorage.getItem('recoFile')); 
      		   	$scope.user.identifications.recomendation = $scope.recoFile.location;
     	   	}
    	}catch(e){}
    };
    
    $scope.init();
};