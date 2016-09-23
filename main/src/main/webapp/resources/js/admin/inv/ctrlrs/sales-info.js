var ModalSalesInfoCtrlr = function ($scope, $uibModalInstance, modalScope, $http, $filter){

	$scope.controlId = _lbGlobalCDNPath;	

	$scope.ms = modalScope;
	$scope.totals = 0;
	$scope.packets = [];
	$scope.locked = false;

	$scope.getTotals = function(){
		$scope.totals = 0;
		for(var i=0;i<$scope.packets.length;i++){
			$scope.totals+=$scope.packets[i].sellingPrice;
		}
		
		$scope.totals+=parseFloat($scope.ms.tip);
	};

	$scope.cancel = function () {
		$uibModalInstance.dismiss($scope.ms);
	};
	
	$scope.ms.updated = false;
	$scope.tipEdit = false;
	$scope.newTip = '';
	$scope.editTip = function(){
		$scope.tipEdit = true;
		$scope.newTip = $scope.ms.tip;
	};
	
	$scope.saveTip = function(){	
		if($scope.locked){
			alert('This sale has been closed. No further edits possible.');
			return;
		}
		
		$scope.ms.tip = $scope.newTip;
		$http.get('/inventory/apps/updatedispatch',{params:{
			mode:'tipupdate',
			t:$scope.ms.tip,
			id:$scope.ms.salesId,
			opsid:_luvbriteGlobalOpsId
			}
		  }).success(function(data){
			  if(data.success){
				  $scope.tipEdit = false;
				  $scope.ms.updated = true;
				
				  $scope.getTotals();
			  }
			  else{
				  alert(data.message);
			  }
		  });
		
	};
	
	$scope.newSplit = "";
	$scope.saveSplitAmt = function(){	
		if($scope.locked){
			alert('This sale has been closed. No further edits possible.');
			return;
		}
		
		$scope.ms.splitAmount = $scope.newSplit;
		$http.get('/inventory/apps/updatedispatch',{params:{
			mode:'splitupdate',
			sa:$scope.ms.splitAmount,
			id:$scope.ms.salesId,
			opsid:_luvbriteGlobalOpsId
			}
		  }).success(function(data){
			  if(data.success){
				  $scope.splitEdit = false;
				  $scope.ms.updated = true;
			  }
			  else{
				  alert(data.message);
			  }
		  });
		
	};
	
	$scope.dateEdit = false;
	$scope.newFinishDate = '';
	$scope.editDate = function(){
		$scope.dateEdit = true;
		
		var dn = new Date(Date.parse($scope.ms.dateFinished));
		$scope.newFinishDate = new Date(dn.getFullYear(),dn.getMonth(),dn.getDate(),dn.getHours(),dn.getMinutes());
	};
	
	$scope.saveDate = function(){		
		if($scope.locked){
			alert('This sale has been closed. No further edits possible.');
			return;
		}
		
		$scope.ms.dateFinished = $filter('date')($scope.newFinishDate, 'MM/dd/yyyy hh:mm a');
		$http.get('/inventory/apps/updatedispatch',{params:{
			mode:'dateupdate',
			d:$scope.ms.dateFinished,
			id:$scope.ms.salesId,
			opsid:_luvbriteGlobalOpsId
			}
		  }).success(function(data){
			  if(data.success){
				  $scope.dateEdit = false;
				  $scope.ms.updated = true;
			  }
			  else{
				  alert(data.message);
			  }
		  });
		
	};
	
	$scope.pmtModeEdit = false;
	$scope.pmtModes = _globalPmtModes;
	$scope.pmtModeSelected = $scope.pmtModes[0];	
	$scope.editPmtMode = function(){
		$scope.pmtModes.forEach(function(x){
			if(x.value == $scope.ms.paymentMode){
				$scope.pmtModeSelected = x;
			}
		});
		$scope.pmtModeEdit = true;	
	};
	
	$scope.savePmtMode = function(){		
		if($scope.locked){
			alert('This sale has been closed. No further edits possible.');
			return;
		}
		
		$http.get('/inventory/apps/updatedispatch',{params:{
			mode:'pmtmodeupdate',
			p:$scope.pmtModeSelected.label,
			id:$scope.ms.salesId,
			opsid:_luvbriteGlobalOpsId
			}
		  }).success(function(data){
			  if(data.success){
				  $scope.pmtModeEdit = false;
				  $scope.ms.updated = true;
				  $scope.ms.paymentMode = $scope.pmtModeSelected.label;
			  }
			  else{
				  alert(data.message);
			  }
		  });
		
	};
	
	$scope.distRecal = function(){		
		if($scope.locked){
			alert('This sale has been closed. No further edits possible.');
			return;
		}
		
		$http.get('/inventory/apps/updatedispatch',{params:{
			mode:'recal_dist',
			id:$scope.ms.salesId,
			opsid:_luvbriteGlobalOpsId
			}
		  }).then(function(resp){
			  var data = resp.data;
			  if(data.success){
				  $scope.ms.distInMiles = parseFloat(data.message);
			  }
			  else{
				  alert(data.message);
			  }
		  });
		
	};
	
	$scope.adjustment = null;
	$scope.adjustmentInProcess = false;
	$scope.addAdjustment = function(){		
		if($scope.adjustmentInProcess) return;
		
		$scope.adjustment = {code:'Adjustment', amount:0, reason:''};
		$('.adjustmentReason').focus();
		$scope.adjustmentInProcess = true;
	};
	
	$scope.saveAdjustment = function(){
		if($scope.locked){
			alert('This sale has been closed. No further edits possible.');
			return;
		}
		
		_lbFns.showBusy();

		$http.get('/inventory/apps/newadjustment',{params:{
			a:$scope.adjustment.amount,
			r:$scope.adjustment.reason,
			id:$scope.ms.salesId,
			d:$scope.ms.dateFinished,
			opsid:_luvbriteGlobalOpsId
			}
		  }).success(function(data){
			  $('div.lboverlay').remove();
			  if(data.success){
				 $scope.adjustmentInProcess = false;	
				 $scope.adjustment = null;
					
				 //reload sale items
				 $scope.loadSaleItems();				 
			  }
			  else{
				  alert(data.message);
			  }
		  })
		  .error(function(){
			  $('div.lboverlay').remove();
		  });
	};
	
	$scope.addPromo = function(code,val){
		if($scope.locked){
			alert('This sale has been closed. No further edits possible.');
			return;
		}
		
		_lbFns.showBusy();

		$http.get('/inventory/apps/newadjustment',{params:{
			a:val,
			r:code,
			id:$scope.ms.salesId,
			d:$scope.ms.dateFinished,
			opsid:_luvbriteGlobalOpsId
			}
		  }).success(function(data){
			  $('div.lboverlay').remove();
			  if(data.success){
					 $scope.adjustmentInProcess = false;	
					 $scope.adjustment = null;
						
					 //reload sale items
					 $scope.loadSaleItems();
			  }
			  else{
				  alert(data.message);
			  }
		  })
		  .error(function(){
			  $('div.lboverlay').remove();
		  });
	};
	
	$scope.addCrtsyPromo = function(){		
		$scope.adjustment = {code:'Adjustment', amount:-10, reason:'PROMOCRTSY'};
		$('.adjustmentReason').focus();
		$scope.adjustmentInProcess = true;
	};
	
	$scope.removeItem = function(){
		if($scope.locked){
			alert('This sale has been closed. No further edits possible.');
			return;
		}
		
		_lbFns.showBusy();

		$http.get('/inventory/apps/updatepacket',{
			params:{
				mode:'remove',
				pc:this.packet.packetCode,
				id:this.packet.id,
				opsid:_luvbriteGlobalOpsId
			}
		  }).then(function(resp){
			  $('div.lboverlay').remove();
			  var data = resp.data;
			  
			  if(data.success){
				  for(var i=0;i<$scope.packets.length;i++){
					  if($scope.packets[i].id==data.packet.id){
						  $scope.packets.splice(i,1);
						  
						  $scope.getTotals();

						  break;
					  }
				  }
			  }
			  else{
				  alert(data.message);
			  }
		  },
		  function(){
			  $('div.lboverlay').remove();
		  });
	};
	
	/* Next Prev Logic */
	$scope.prevSalesId = 0;
	$scope.nextSalesId = 0;
	
	var prevNextLogic = function(){
		$scope.prevSalesId = 0;
		$scope.nextSalesId = 0;
		
		if(!$scope.ms.nextPrevDispatches) return;
		
		var length = $scope.ms.nextPrevDispatches.length,
		lastIndx = length?length-1:0;
		
		if(lastIndx){
			for(var i=0; i<length; i++){
				if($scope.ms.nextPrevDispatches[i] == $scope.ms.salesId){
					if(i !=0 ){
						$scope.prevSalesId = $scope.ms.nextPrevDispatches[i-1];
					}
					
					if(i != lastIndx){
						$scope.nextSalesId = $scope.ms.nextPrevDispatches[i+1];
					}
					
					break;
				}
			}
		}
	};
	
	//Check for the logic during initial load;
	prevNextLogic();
	
	$scope.getNextSales = function(){
		if($scope.nextSalesId) getNewSalesInfo($scope.nextSalesId);
	};
	
	$scope.getPrevSales = function(){
		if($scope.prevSalesId) getNewSalesInfo($scope.prevSalesId);
	};	
	
	/* Next Prev Logic Ends */
	
	

	/* Fetch new sales details */
	var getNewSalesInfo = function(salesId){
		
		$scope.ms.salesId = 0;
		$scope.packets = [];
		
		$http.get('/inventory/apps/listdispatches?id='+salesId).then(function(resp){
			var data = resp.data;
			if(data.success){
			
				var dispatches = data.result;
				
				$scope.ms.salesId = salesId;
				$scope.ms.dateFinished = dispatches[0].dateFinished;
				$scope.ms.dateArrived = dispatches[0].dateArrived;
				$scope.ms.clientName = dispatches[0].clientName;
				$scope.ms.paymentMode = dispatches[0].paymentMode;
				$scope.ms.additionalInfo = dispatches[0].additionalInfo;
				$scope.ms.driverName = dispatches[0].driverName;
				$scope.ms.tip = dispatches[0].tip;
				$scope.ms.splitAmount = dispatches[0].splitAmount;
				$scope.ms.commissionPercent = dispatches[0].commissionPercent;
				$scope.ms.distInMiles = dispatches[0].distInMiles;
				$scope.ms.status = dispatches[0].status;
				
				$scope.loadSaleItems();
				
				prevNextLogic();

				checkStatus();
			}
		});		
	};
	/* Fetch new sales details ends */

	var checkStatus = function(){		
		$scope.locked = false;
		if($scope.ms.status && $scope.ms.status=='locked') {
			$scope.locked = true;
		}
	};
	//Initial load
	checkStatus();
	
	
	$scope.loadSaleItems = function(){
		
		$http.get('/inventory/apps/listpackets?s='+$scope.ms.salesId).success(function(data){
			if(data.success){
				$scope.packets = data.result;
				$scope.getTotals();
			}
			else{
				alert(data.message);
			}
		});		
	};
	
	
	/* Initial Load*/
	$scope.loadSaleItems();
};