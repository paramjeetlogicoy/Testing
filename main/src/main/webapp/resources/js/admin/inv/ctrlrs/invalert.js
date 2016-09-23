var invAlertCtrlr = function($scope, $http, $rootScope){

	$rootScope.rootPage = "Inv Alert";
	$scope.controlId = _luvbriteGlobalOpsId;
	
	if($scope.controlId>100) return;	
	
	$scope.productSearch = "";
	$scope.alerts = [];
	$http.get('/inventory/apps/invalert/list').success(function(data){
		if(data.success && data.result.length > 0){
			$scope.alerts = data.result;
		}
	});
	
	$scope.showSave=function(){
		$(event.target).parents('.inv-alert-rows').find("a.inv-save-alert .glyphicon").fadeIn(); 
	};
	
	$scope.saveAlert=function(){
		
		var item = this.alert;
		$scope.elem = $(event.target);
		
		$scope.elem.attr("class","glyphicon glyphicon-floppy-open")
		
		$http.get('/inventory/apps/invalert/update',{
			params: {
				id:item.productId, 
				af:item.alertActive, 
				al:item.amberLevel, 
				wl:item.redLevel, 
				opsid:_luvbriteGlobalOpsId
			}
		})
		.success(function(res){
			
			if(res.success){				
				$scope.elem.attr("class","glyphicon glyphicon-floppy-saved").fadeOut(1500, function(){
					$(this).attr({"class":"glyphicon glyphicon-floppy-disk"});
				});
			}
			else{
				alert(res.message);
			}
		}).error(function(){
			alert("There was some error removing the return.");
		});
	};
	
};