var addCategoryCtrlr = function($scope, $http, $filter, $rootScope, currentUser){

	$rootScope.rootPage = "Categories";
	$scope.controlId = currentUser.ctrlid;
	
	$scope.currentCategoryId = 0;
	$scope.btnText = 'ADD';
	$scope.categories = [];
	
	$http.get('/inventory/apps/listcats').success(function(data){
		if(data.success){
			$scope.categories = data.result;
		}
	});
	
	$scope.addCategory=function(){
		if($scope.currentCategoryId!=0){
			
			for(var i=0;i<$scope.categories.length;i++){
				if($scope.categories[i].id==$scope.currentCategoryId){
					$scope.categories[i].categoryName = $scope.categoryName;

					break;
				}
			}
			
			$.post('/inventory/apps/updatecat',{id:$scope.currentCategoryId, cn:$scope.categoryName, opsid:_luvbriteGlobalOpsId},
			function(data){
				if(!data.success) alert(data.message);
		    });			
			
			$scope.currentCategoryId = 0;
			$scope.categoryName = '';
			$scope.btnText = 'ADD';
		}
		else {
			$scope.categories.push({categoryName:$scope.categoryName, formattedDateAdded: $filter('date')(new Date(), 'MM/dd/yyyy hh:mm a')});
			
			$.post('/inventory/apps/newcat',{cn:$scope.categoryName, opsid:_luvbriteGlobalOpsId},function(data){
				if(data.success){
					for(var i=0;i<$scope.categories.length;i++){
						if($scope.categories[i].categoryName==data.cat.categoryName){
							$scope.categories[i] = data.cat;
							try{$scope.$apply();}catch(err){}
							break;
						}
					}
				}
				else{
					alert(data.message);
				}
		    });
			
			$scope.categoryName = '';		
		}
	};
	
	$scope.editCategory=function($event){
		$event.preventDefault();
		
		$scope.categoryName = this.category.categoryName;
		$scope.currentCategoryId = this.category.id;
		$scope.btnText = 'SAVE';
		
		/**
		 * Scroll up to the form and show subtle color change.
		 **/
		_lbFns.showViewForm();
	};
	
	$scope.cancelEdit=function($event){
		$event.preventDefault();
		
		$scope.currentCategoryId = 0;
		$scope.categoryName = '';
		$scope.btnText = 'ADD';
	};
	
};