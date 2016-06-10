/**
 * This service is used for all FileUploads
 * Angular module is defined all the way at the bottom
 */

var uploadServiceFn = function($templateRequest, $compile){
	
	var service = {};
	
	service.showGallery = function(scope){
		//Show popup		
		$templateRequest("/resources/ng-templates/admin/file-gallery.html")
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('body').addClass('noscroll').append(template);
		      $compile(template)(scope);
		  });
	};
	
	service.selectedFiles = [];
	
	service.cdnPath = _lbGlobalCDNPath; //defined in <head />
	
	service.config = {
			"cb" : null, 	//callback fn
			"fields" : {path : '', ctrl : ''},
			"listFiles" : true, //Whether the provide option to listFiles
			"multipleSelect" : false   //Select only one file at a time
	};
	
	return service;	
},

uploadModule = angular.module('uploadModule', []);
uploadModule.factory('uploadService', ['$templateRequest', '$compile', uploadServiceFn]);



var uploadCtrlr = function ($scope, $http, Upload, uploadService) {
	
	$scope.sfiles = [];
	$scope.search = '';
	$scope.cdnPath = uploadService.cdnPath;
	
    $scope.$watch('files', function () {
        $scope.upload($scope.files);
    });
    
    $scope.hideGallery = function(){
		angular.element('.pop-gallery-overlay').remove();
		angular.element('body').removeClass('noscroll');
	};
	
	$scope.loadFiles = function(){
		$http.get('/files/list/',{params :{
			'c' : $scope.sfiles.length,
			'l' : 15 ,
			'q' : $scope.search
		}})
		.success(function(data){
			if(!data || data.length == 0){
				angular.element('.fgw-file-list>ul').unbind("scroll");
			}
			else
				Array.prototype.push.apply($scope.sfiles, data);
		})
		.error(function(){
			alert('There was some error getting the files');
		});
		
	};

	if(uploadService.config.listFiles) {
		$scope.loadFiles();
		
		angular.element('.fgw-file-list>ul').bind("scroll", function() {
			var $e = $(this);
			if($e.scrollTop() + $e.innerHeight() >= $e[0].scrollHeight){
				$scope.loadFiles();
            }
		});
	}
	
	
	$scope.selectFiles = function(){
		
		if(this.file.selected) {
			this.file.selected = false;
			return;
		}
		
		if(!uploadService.config.multipleSelect){
			$scope.sfiles.forEach(function(e){
				if(e.selected) e.selected = false;
			});
		}
		
		this.file.selected = true;
	};
	
	
	$scope.useSelectedFiles = function(){
		var selectedFiles = [];
		$scope.sfiles.forEach(function(e){
			if(e.selected) selectedFiles.push(e);
		});
		
		if(selectedFiles.length>0){
			uploadService.selectedFiles = selectedFiles;
			$scope.hideGallery();
			
			if(uploadService.config.cb) uploadService.config.cb();
		}
		else{
			alert('No files selected');
		}
	}
    
    // for multiple files:
    $scope.upload = function (files) {
      if (files && files.length) {
        for (var i = 0; i < files.length; i++) {

            var file = files[i];
	        var fileInfo = {progress : false, element : null};
	        
	        Upload.upload({
	            url: '/files/upload',
	            fields : uploadService.config.fields, //{path : 'test/', ctrl : ''},
	        	file: file,
	        	fileInfo : fileInfo
	            
	        }).then(

	    	        function (resp) { //Success
		    	        
			            if(resp.config.fileInfo)
			            	resp.config.fileInfo.element.closest('.row').remove();
			
			           if($scope.sfiles && resp.data.results){
			        	   $scope.sfiles.unshift(resp.data.results[0])
			           }
			            
			        },



	    	        function (resp) {//Error
			            if(resp.config.fileInfo)
			            	resp.config.fileInfo.element.html('Error uploading the file');
			        },

	
	    	        function (evt) { //Progress
			        	if(!evt.config.fileInfo.progress){
				        	var f = evt.config.fileInfo;
				        	
			        		f.progress = true;
			        		f.element = $('<div />').attr({'class': 'progress-bar progress-bar-success', 'role':'progressbar','style':'width:0%'});
			        		
			        		$('<div class="row"><div class="col-xs-4 file-name-holder">' 
			                		+ evt.config.file.name 
			                		+ '</div><div class="col-xs-8"><div class="progress"></div></div>')
			                		.appendTo('.progress-wrapper')
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
      }
    }
};