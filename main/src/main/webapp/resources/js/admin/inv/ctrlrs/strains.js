var addStrainsCtrlr = function($scope, $http, $filter, $rootScope, currentUser){
	
	$rootScope.rootPage = "Strains";	
	$scope.controlId = currentUser.ctrlid;
	
	$scope.currentStrainId = 0;
	$scope.btnText = 'ADD';
	$scope.strains = [];
	
	$http.get('/inventory/apps/liststrains').success(function(data){
		if(data.success){
			$scope.strains = data.result;
		}
	});
	
	$scope.addStrain=function(){
		if($scope.currentStrainId!=0){
			
			for(var i=0;i<$scope.strains.length;i++){
				if($scope.strains[i].id==$scope.currentStrainId){
					$scope.strains[i].strainName = $scope.strainName;

					break;
				}
			}
			
			$.post('/inventory/apps/updatestrain',{id:$scope.currentStrainId, sn:$scope.strainName, opsid:_luvbriteGlobalOpsId},
			function(data){
				if(!data.success) alert(data.message);
		    });			
			
			$scope.currentStrainId = 0;
			$scope.strainName = '';
			$scope.btnText = 'ADD';
		}
		else {
			$scope.strains.push({strainName:$scope.strainName, formattedDateAdded: $filter('date')(new Date(), 'MM/dd/yyyy hh:mm a')});
			
			$.post('/inventory/apps/newstrain',{sn:$scope.strainName, opsid:_luvbriteGlobalOpsId},function(data){
				if(data.success){
					for(var i=0;i<$scope.strains.length;i++){
						if($scope.strains[i].strainName==data.strain.strainName){
							$scope.strains[i] = data.strain;
							try{$scope.$apply();}catch(err){}
							break;
						}
					}
				}
				else{
					alert(data.message);
				}
		    });
			
			$scope.strainName = '';		
		}
	};
	
	$scope.editStrain=function($event){
		$event.preventDefault();
		
		$scope.strainName = this.strain.strainName;
		$scope.currentStrainId = this.strain.id;
		$scope.btnText = 'SAVE';
		
		/**
		 * Scroll up to the form and show subtle color change.
		 **/
		_lbFns.showViewForm();
	};
	
	$scope.cancelEdit=function($event){
		$event.preventDefault();
		
		$scope.currentStrainId = 0;
		$scope.strainName = '';
		$scope.btnText = 'ADD';
	};
	
};