<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%@ include file="../common/meta.jsp"%>
<%@ include file="../common/header.jsp"%>

<script type="text/javascript">

window.connectionNode;

Ext.Loader.setConfig({
    enabled: true
});
Ext.Loader.setPath('Ext.ux', '<c:url value="/resources/extjs/ux"/>');
Ext.require([
             'Ext.tab.*',
             'Ext.ux.TabCloseMenu'
]);
Ext.onReady(function() {
	showHistoryWin = function() {
		var win = Ext.getCmp('query-history-window');
		if(!win){
			var historyList = Ext.create('Ext.ListView', {
				title: 'Query',
				hideHeaders: false, padding : 0,
				border: true, region: 'north', 
				id : 'query-history-list',
				height: 250, split: true,
				store : Ext.create('Ext.data.JsonStore', {
					fields : [ 'taskId', 'query', 'status', 'finished', 'startTime', 'endTime', 'errorMessage' ],
					proxy : Ext.data.ScriptTagProxy({ 
						type : 'ajax',
						url : APPLICATION_CONTEXT + 'hive/getQueryTasks.do'
					})
				}),
				columns : [ { width : 100, text: 'Task Id', dataIndex : 'taskId' }, 
							{ flex: 1, text: 'Query', dataIndex : 'query' },
							{ width: 100, text: 'Status', dataIndex : 'status' },
							{ width: 100, text: 'Finished', dataIndex : 'finished' },
							{ width: 100, text: 'Start time', dataIndex : 'startTime', renderer: function(value) { if(value != null) { return Ext.util.Format.date(new Date(value), 'Y-m-d H:i:s');} } },
							{ width: 100, text: 'End time', dataIndex : 'endTime', renderer: function(value) { if(value != null) {return Ext.util.Format.date(new Date(value), 'Y-m-d H:i:s');} } },
							{ width: 100, text: 'Error', dataIndex : 'errorMessage' }
							],
				listeners: {
					'cellclick' : function(iView, iCellEl, iColIdx, iRecord, iRowEl, iRowIdx, iEvent) {
						var fieldName = iView.getGridColumns()[iColIdx].dataIndex;
						if(fieldName == 'errorMessage') {
							if(iRecord.get('errorMessage') != '') {
								alert(iRecord.get('errorMessage'));
							}
						}
					},
					'itemclick' : function(grid, record, e) {
						Ext.getCmp('hisory-result-grid').store.loadData([]); 
						Ext.getCmp('hisory-result-grid').store.load({
							params: {
								taskId: record.get('taskId')
							}
						}); 
					}
				},
		        tbar: ['->', {
		            text: 'Reload',
		            iconCls: 'x-tbar-loading',
		            handler: function() {
		        		Ext.getCmp('query-history-list').store.load();        
		            }
		        }]
												
			});	

			var resultGrid =  {
					xtype: 'gridpanel', autoScroll: true,
					id: 'hisory-result-grid',
					title: 'Result',
					region: 'center', layout:'fit',
					height: 500,
					split: true, border: true,
					store : Ext.create('Ext.data.Store', {
						fields : [],
			            proxy: {
						        type: 'ajax',
						        url : APPLICATION_CONTEXT+'hive/getQueryResult.do',
						        reader: {
						            type: 'json',
						            root: 'data'
						        },
								listeners: {
					                exception : Drone.exceptionListener
						        } 	
					    },
					    listeners: {
							load: function(store, meta){
								var grid = Ext.getCmp('hisory-result-grid');
								grid.reconfigure(store, store.proxy.reader.fields);
								//console.log(grid.columns.length);
								//console.log(grid.columns[grid.columns.length - 1]);
								//grid.columns[grid.columns.length - 1].flex = true;
							}
						}
					}),
					columns : [],
					tbar: ['->', {
			        	text: 'Download result',
			        	//icon : APPLICATION_CONTEXT + 'resources/images/icons/fam/download.png',		
			        	iconCls:'save',	        	
			            handler: function() {
			            	if(Ext.getCmp("query-history-list").getSelectionModel().getLastSelected() == null) {
			            		alert('selec task');
			            		return;
			            	}
			            	var taskId = Ext.getCmp('query-history-list').getSelectionModel().getLastSelected().get('taskId');
			            	if(taskId == null) {
				            	alert('selec task');
				            	return;
			            	}
			            	showDownloadWindow(taskId);
			            }
			        }]
			};
					
			win = Ext.widget('window', {
	            title: 'Query history',
				id: 'query-history-window',
	            width: 800, height: 600, layout: 'border', resizable: false, modal: true,
	            buttons: [{
	                text: 'Close',
	                handler: function() {
	                    this.up('window').hide();
	                }
	            }],
	            items: [
	    	    	historyList,
	    	    	resultGrid
	    	    ]
			});
		}
		win.show();    
		Ext.getCmp('query-history-list').store.load();        
	};
	
	showCreateConnectionWindow = function(cmp_id){
		var win = Ext.getCmp(cmp_id);
		if(!win){
			var win = Ext.widget('window', {
	            title: 'Create Hive Connection',
				id: cmp_id,
				width: 650,
	            autoHeight: true,
	            resizable: true,
	            modal: true,
	            onEsc: function(){
	            	this.close();
	            },
	            items: createHiveConnectionCmp()
	        });
		}else{
			win.center();
		}
		win.show();
	};
	
	submitDeleteConnectionTreeNode = function(selNode){
		var confirm =Ext.MessageBox.confirm(
	  			'Are you sure?', 
				'Please confirm the deletion of ' + selNode.get('text'),
				function(btn){
	  					if (btn != 'yes') return;
						
	  					Ext.Ajax.request({
	  				      	url: APPLICATION_CONTEXT+'hive/removeConn.do',
	  				      	method: 'POST',
	  				      	params : {
	  				      		connectionId : selNode.get('connectionId'),
	  				      		connectionName : selNode.get('connectionName')
	  				      	},
	  				      	success: function(response, opts) { 
	  				      		var responseJson = Ext.decode(response.responseText);
	  				      		if (responseJson.success === true) { 
	  				      			var treePanel = Ext.getCmp('hive-connection-tree-cmp'); 
	  				       			treePanel.getSelectionModel().deselect(selNode);
	  				       			selNode.remove();
	  				       			Drone.msg(responseJson.msg, '');
	  				      		} else {
	  				      			Ext.Msg.alert('failure', responseJson.msg);
	  				      		}
	  				      	},
	  				      	failure: Drone.onFailureAjax
			      	});
	  			  
			  });
	};

	createRightCmp = function() {
		return Ext.create('Ext.Panel', {
			layout: 'fit', split: true, width: 350,
			items: [
				createQueryManagerListCmp()
			]
		});
	};

	createConnectionInfoCmp = function() {
		return Ext.create('Ext.form.Panel', {
	       	id: 'hiveConnection-detail-form', 
			frame: true, bodyPadding: 5, border :false, waitMsgTarget: true, defaultType: 'textfield',
	        fieldDefaults: { labelAlign: 'left', labelWidth: 100, anchor: '100%' },
	        items: [{ name: 'connectionName', fieldLabel: 'Name', allowBlank: false, labelWidth: 80, readOnly: true },
	                { name: 'driverClass', fieldLabel: 'Driver Class', allowBlank: false, labelWidth: 80 },
	     	        { name: 'connectionUri', fieldLabel: 'Connection URI', allowBlank: false, labelWidth: 80},
	     	        { name: 'fileSystemUri', fieldLabel: 'FileSystem URI', allowBlank: true, labelWidth: 80},
	     	        { xtype: 'textareafield', name: 'description', fieldLabel: 'Description', labelAlign: 'top', flex: 1, margins: '0', allowBlank: true }],
   	       	buttons: [ { text: 'Save', disabled: true, formBind: true, 
	   			         handler: function() {
   			            	if (!this.up('form').getForm().isValid()){
   			            		return;
   			            	}
   			            	var serverJson = this.up('form').getForm().getFieldValues();
   			            	var formPanel = this.up('form');
   			            	var confirm = Ext.MessageBox.confirm('Are you sure?', 'Please confirm the saving of ' + serverJson['connectionName'],
   		            			function(btn){
   	              					if (btn != 'yes') return;

   	              					formPanel.getForm().submit({
   	                        	    	clientValidation: true,
   	                        	    	url: APPLICATION_CONTEXT + 'hive/saveConn.do',
   	                        	    	params: {
   				            	        	params: Ext.encode(serverJson)
   				            	    	},
   	                        	    	success: function(form, action) {
   	                        	    		Drone.msg(action.result.msg, '');
   	                        	    		Ext.getCmp('hive-connection-tree-cmp').store.proxy.extraParams.category = '';	
   	                        	    		Ext.getCmp('hive-connection-tree-cmp').store.load();
                               	    		if(window.connectionNode != null) {
                               	    			window.connectionNode.expand();
                               	    		}
   	                        	    	},
   	                        	    	failure: function(form, action) {
   	                        	    		Drone.onSuccessOrFailForm(form, action);
   	                        	    	}
   	                        		});	//form submit
   		            		  }); //btn function
   			             } //handler
   			        }] //buttons	     	        
		});	     	        
	};
	
	createLeftCmp = function() {
		var tab = Ext.create('Ext.TabPanel', {
			region: 'south', layout: 'fit', deferredRender: false, margins: '0 0 0 0',
	    	border : false, hideCollapseTool : true, activeTab: 0,
	    	split: true,
	        items: [{									    	
		        hideMode : Ext.isIE ? 'offsets' : 'display',
		    	layout: 'fit',
				title: 'Columns',
		        items: [ createColumnListCmp() ]
	        }, {
		        title: 'Connection',
		        hideMode : Ext.isIE ? 'offsets' : 'display',
		    	layout: 'fit',
		        items: [ createConnectionInfoCmp() ]
	        }]
		});		
		
		return Ext.create('Ext.Panel', {
			layout: 'border', split: true,
			items: [
				createHiveConnectionTreeCmp(),
				tab
			]
		});
	};
	
	createHiveConnectionCmp = function(){ 
		return Ext.create('Ext.form.Panel', {
	        id: 'hiveConnection-create-form', 
			frame: true, bodyPadding: 5, border :false, waitMsgTarget: true, defaultType: 'textfield',
	        fieldDefaults: { labelAlign: 'left', labelWidth: 100, anchor: '100%' },
	        items: [{ name: 'connectionName', fieldLabel: 'Connection Name', allowBlank: false, labelWidth: 100 },
	                { name: 'driverClass', fieldLabel: 'Driver Class', allowBlank: false, labelWidth: 100, value: 'org.apache.hadoop.hive.jdbc.HiveDriver' },
	     	        { name: 'connectionUri', fieldLabel: 'Connection URI', allowBlank: false, labelWidth: 100, value: 'jdbc:hive://localhost:10000/default'},
	     	        { name: 'fileSystemUri', fieldLabel: 'FileSystem URI', allowBlank: true, labelWidth: 100, value: 'hdfs://127.0.0.1:9000'},
	     	        { xtype: 'textareafield', name: 'description', fieldLabel: 'Description', labelAlign: 'top', flex: 1, margins: '0', allowBlank: true } ],
	     	buttons: [ { text: 'Close', handler: function() { this.up('window').destroy(); } },
	     		       { text: 'Add', disabled: true, formBind: true, 
			             handler: function() {
			            	if (!this.up('form').getForm().isValid()){
			            		return;
			            	}
			            	var serverJson = this.up('form').getForm().getFieldValues();
			            	var formPanel = this.up('form');
			            	var confirm = Ext.MessageBox.confirm('Are you sure?', 'Please confirm the creation of ' + serverJson['connectionName'],
		            			function(btn){
	              					if (btn != 'yes') return;

	              					formPanel.getForm().submit({
	                        	    	clientValidation: true,
	                        	    	url: APPLICATION_CONTEXT + 'hive/createConn.do',
	                        	    	params: {
				            	        	params: Ext.encode(serverJson)
				            	    	},
	                        	    	success: function(form, action) {
	                        	    		formPanel.up('window').destroy();
	                        	    		Drone.msg(action.result.msg, '');
	                        	    		Ext.getCmp('hive-connection-tree-cmp').store.proxy.extraParams.category = '';	
	                        	    		Ext.getCmp('hive-connection-tree-cmp').store.load();
                            	    		if(window.connectionNode != null) {
                            	    			window.connectionNode.expand();
                            	    		}
	                        	    	},
	                        	    	failure: function(form, action) {
	                        	    		Drone.onSuccessOrFailForm(form, action);
	                        	        	formPanel.up('window').destroy();
	                        	    	}
	                        		});	//form submit
		            		  }); //btn function
			             } //handler
			        }] //buttons
	    }); //create
	};

	getSelectedTreeNode = function(cmp_id){
		var cmp = Ext.getCmp(cmp_id);
		if(cmp) return Ext.getCmp(cmp_id).getSelectionModel().getLastSelected();
		return null;
	};
	
	showAddCategoryWindow = function() {
		var win = Ext.getCmp('query-category-add-window');
		if(!win) {
			win = Ext.widget('window', {
	            title: 'Query history',
				id: 'query-category-add-window',
	            width: 800, height: 100, layout: 'border', resizable: false, modal: true,
	            title: 'Save Query', width: 650, layout: 'border',
	            autoHeight: true, resizable: true, modal: true,
	            onEsc: function() { this.close(); },
	            items: [{ xtype:'form', id: 'add-category-form', region: 'center',
	        			frame: true, bodyPadding: 5, border :false, waitMsgTarget: true, defaultType: 'textfield',
	        	        fieldDefaults: { labelAlign: 'left', labelWidth: 100, anchor: '100%' },
	        	        items: [{ name: 'categoryName', fieldLabel: 'Category Name', allowBlank: false, labelWidth: 100}],
	        	     	buttons: [ { text: 'Close', handler: function() { this.up('window').destroy(); } },
	        	     		       { text: 'Add', disabled: true, formBind: true, 
	        			             handler: function() {
	        			            	if (!this.up('form').getForm().isValid()){
	        			            		return;
	        			            	}
	        			            	var serverJson = this.up('form').getForm().getFieldValues();
	        			            	var formPanel = this.up('form');
	        			            	var confirm = Ext.MessageBox.confirm('Are you sure?', 'Please confirm the saving of ' + serverJson['categoryName'],
	        		            			function(btn){
	        	              					if (btn != 'yes') return;

	        	              					formPanel.getForm().submit({
	        	                        	    	clientValidation: true,
	        	                        	    	url: APPLICATION_CONTEXT + 'hive/addQueryCategory.do',
	        	                        	    	params: {
	        				            	        	params: Ext.encode(serverJson)
	        				            	    	},
	        	                        	    	success: function(form, action) {
	        	                        	    		formPanel.up('window').destroy();
	        	                        	    		Ext.getCmp('hive-query-list-cmp').stor.load();
	        	                        	    		Drone.msg(action.result.msg, '');
	        	                        	    	},
	        	                        	    	failure: function(form, action) {
	        	                        	    		Drone.onSuccessOrFailForm(form, action);
	        	                        	        	formPanel.up('window').destroy();
	        	                        	    	}
	        	                        		});	//form submit
	        		            		  }); //btn function
	        			             } //handler
	        			        }] //buttons
	            		}]//form
	        	    }); //create	    	            
		} else{
			win.center();
		}
		win.show();
	};
	
	createQueryManagerListCmp = function(){
	    var tree = Ext.create('Ext.ListView', {
	        id: 'hive-query-list-cmp',
	        hideHeaders : false,
			header : false,
			forceFit: true,
			border : false,
			flex: 1,
			reserveScrollOffset: true,
			region: 'center',
			store: Ext.create('Ext.data.JsonStore', {
				fields : [  { name: 'categoryName', type: 'string'},
				            { name: 'queryName', type: 'string'},
				            { name: 'queryId'},
				            { name: 'query', type: 'string'},
				             ],
				proxy : Ext.data.ScriptTagProxy({ 
					type : 'ajax',
					url : APPLICATION_CONTEXT + 'hive/getQueryListForCategory.do'
				}),
				listeners: {
				}
			}),
			columns: [
				{header: "Category", width:100, dataIndex: 'categoryName', renderer: function (value, meta, record, rowIndex, colIndex, store) {
			        var first = !rowIndex || value !== store.getAt(rowIndex - 1).get('categoryName'),
			            last = rowIndex >= store.getCount() - 1 || value !== store.getAt(rowIndex + 1).get('categoryName');
			        meta.css += 'row-span' + (first ? ' row-span-first' : '') +  (last ? ' row-span-last' : '');
			        if (first) {
			            var i = rowIndex + 1;
			            while (i < store.getCount() && value === store.getAt(i).get('categoryName')) {
			                i++;
			            }
			            var rowHeight = 20, padding = 6,
			                height = (rowHeight * (i - rowIndex) - padding) + 'px';
			            meta.attr = 'style="height:' + height + ';line-height:' + height + ';"';
			        }
			        return first ? value : ''} },
				{header: "Query name", flex:1, dataIndex: 'queryName'},
				{header: "Query", width: 80, dataIndex: 'query'}
			], 
			listeners: {
	    		'itemclick' : function(grid, record, e) {
    				var query = record.get('query');
    				if(query != '') {
    					var tab = Ext.getCmp('query-tab-cmp')
    					var idx = tab.items.indexOf(tab.getActiveTab());
    					Ext.getCmp('query-textarea' + (idx + 1)).setValue(query);
    				}
	    		},
				'cellclick' : function(iView, iCellEl, iColIdx, iRecord, iRowEl, iRowIdx, iEvent) {
					/*
					var fieldName = iView.getGridColumns()[iColIdx].dataIndex;
					if(fieldName == 'query') {
						if(iRecord.get('query') != '') {
							alert(iRecord.get('query'));
						}
					}
					*/
				}				
	    	},    
			dockedItems: [{
	    	    xtype: 'toolbar',
	    	    dock: 'top',
	    	    items: [{
	            	text   : 'Add Category',
	            	iconCls : 'add',
	                handler: function() {
	                	showAddCategoryWindow();
	                }
	            },'-',
	            {
	                  text   : 'Delete',
	                  iconCls: 'remove',
	                  handler: function() {
  	            		submitDeleteQueryCategory();
	                  }
	            },'->',
	            {
	                  text   : 'Reload',
	                  iconCls: 'x-tbar-loading',
	                  handler: function() {
	                  	Ext.getCmp('hive-query-list-cmp').store.load();
	                  }
	              }]
	    	} ]
	    }); 

	    Ext.getCmp('hive-query-list-cmp').store.load();
	    return tree;
	};

	submitDeleteQueryCategory = function() {
		if(Ext.getCmp('hive-query-list-cmp').getSelectionModel().getLastSelected() == null) {
			alert('select query');
			return;
		}

		var row = Ext.getCmp('hive-query-list-cmp').getSelectionModel().getLastSelected();
		var categoryName = row.get('categoryName');
		var queryName = row.get('queryName');
		var queryId = row.get('queryId');
		
		var message = 'If delete category. All queries in category be deleted.';
		if(queryName != null && queryName != '') {
			message = 'Please confirm deleting query [' + queryName + ']';
		} 
		var confirm = Ext.MessageBox.confirm('Are you sure?', message ,
    			function(btn){
  					if (btn != 'yes') return;
  					
					Ext.Ajax.request({
				        url: APPLICATION_CONTEXT + 'hive/removeQuery.do',
				        method: 'POST',
				        params: {
				        	categoryName : categoryName,
				        	queryId: queryId
				        },
				        success: function(response, opts) {
				        	Ext.getCmp('hive-query-list-cmp').store.load();
				        },
				        failure: function(response, opts) {
				        	Drone.onFailureAjax(response);
				        } 
					});	
				}
		);	
	}
	
	createHiveConnectionTreeCmp = function(){
	    return {
	    	xtype: 'treepanel',
	    	region:'center', 
	        id: 'hive-connection-tree-cmp',
	        rootVisible: false,
	        autoScroll: true,
	        border: true,
	        singleExpand: true,
	        store: Ext.create('Ext.data.TreeStore', {
	            proxy: {
	                type: 'ajax',
	                url: APPLICATION_CONTEXT+'hive/tree.do',
	                extraParams: {
	                	category: '',
	                	value: '',
	                	connectionName: ''
	                }
	            },
	            sorters: [{
	                property: 'text',
	                direction: 'ASC'
	            }],
	            fields : [ {name:'value', type:'string'},
	       	               {name:'text', type:'string'}, 
	                       {name:'connectionName', type:'string'}, 
				           {name:'category', type:'string'},
				           {name:'hiveConn'}
				          /* {name:'connectionUri', type:'string'},
				           {name:'driverClass', type:'string'},
				           {name:'fileSystemUri', type:'string'},
				           {name:'description', type:'string'}*/
				]
	        }),
	        listeners: {
				'itemcollapse': function(node) {
					node.set('loaded', false); //clear node data cache
					while(node.firstChild) {
						  node.removeChild(node.firstChild);
					}
				},
				'beforeload': function( store, operation, eOpts ) {
					if( "" != operation.node.data.category ) {
						store.proxy.extraParams.category = operation.node.get('category');
					}
					if( "" != operation.node.data.value ) {
						store.proxy.extraParams.value = operation.node.get('value');
					}
					if( "" != operation.node.data.connectionName ) {
						store.proxy.extraParams.connectionName = operation.node.get('connectionName');
					}
				}
			},
			dockedItems: [{
	    	    xtype: 'toolbar',
	    	    dock: 'top',
	    	    items: [{
	            	text   : 'Add',
	            	iconCls : 'add',
	                handler: function() {
	                	showCreateConnectionWindow('create-hive-connection-window');
	                }
	            },'-',
	            {
	                  text   : 'Delete',
	                  iconCls: 'remove',
	                  handler: function() {
	                	var selNode = getSelectedTreeNode('hive-connection-tree-cmp');
	  	            	if(!selNode) {
							alert('select node');
		  	            	return;
	  	            	}
	  	            	if(selNode.get('category') == 'connection'){
	  	            		submitDeleteConnectionTreeNode(selNode);
	  	            	} else {
		  	            	alert('Can\'t delete');
		  	            	return;
	  	            	}
 	                  }
	              }]
	    	} ]
	    }; 
	};
	
	createColumnListCmp = function(){ 
		return {
			id: 'column-grid-cmp',
			region:'south',
			xtype: 'gridpanel',
			split: true,
			autoScroll: true,
			border: true,
			title: 'Columns',
			height: 300,
			layout: 'fit',
	        forcefit: true,
			store : Ext.create('Ext.data.Store', {
				fields: [{name: 'columnName', type: 'string'},
				         {name: 'dataType', type: 'string'},
			             {name: 'comment', type: 'string'}
		         ],
	             proxy: {
				        type: 'ajax',
				        url : APPLICATION_CONTEXT + 'hive/tableDetail.do',
						listeners: {
			                exception : Drone.exceptionListener
				        },
				        reader: {
				            type: 'json',
				            root: 'columns'
				        }
			    	}
			}),
			viewConfig : {
				//disableSelection : true
			},
			columns : [{
	            header: 'Name',
	            dataIndex: 'columnName',
	            flex: 1
	        },{
	            header: 'Type',
	            dataIndex: 'dataType',
	            width: 80,
	            //align: 'center'
	        }]
		};
	};

	showDownloadWindow = function(taskId) {
		var win = Ext.getCmp('query-result-file-window');
		if(!win){
			var fileList = Ext.create('Ext.ListView', {
				hideHeaders: false, padding : 0,
				border: true, region: 'center', 
				id : 'query-result-file-list',
				height: 250, split: true,
				store : Ext.create('Ext.data.JsonStore', {
					fields : [ 'path', 'createTime', 'length'],
					proxy : Ext.data.ScriptTagProxy({ 
						type : 'ajax',
						url : APPLICATION_CONTEXT + 'hive/getQueryResultFiles.do'
					})
				}),
				columns : [ { flex: 1, text: 'name', dataIndex : 'path' },
							{ width: 150, text: 'date', dataIndex : 'createTime', renderer: function(value) { if(value != null) { return Ext.util.Format.date(new Date(value), 'Y-m-d H:i:s');} } },
							{ width: 100, text: 'length', dataIndex : 'length' }
						],
				listeners: {
					'itemclick' : function(grid, record, e) {
					}
				},
		        tbar: ['->', {
		            text: 'Download',
		            //icon : APPLICATION_CONTEXT + 'resources/images/icons/fam/download.png',
		            iconCls:'save',
		            handler: function() {
			        	var path = Ext.getCmp('query-result-file-list').getSelectionModel().getLastSelected().get('path');
						var form = Ext.getCmp('hive-download-form').getForm();
		        		form.findField('taskId').setValue(taskId);
		        		form.findField('path').setValue(path);
		        		form.target = 'hive-download-iframe'; 
		        		form.submit();		            
		            }
		        }]
												
			});	
					
			win = Ext.widget('window', {
	            title: 'Query result files',
				id: 'query-result-file-window',
	            width: 600, height: 400, layout: 'border', resizable: false, modal: true,
	            buttons: [{
	                text: 'Close',
	                handler: function() {
	                    this.up('window').hide();
	                }
	            }],
	            items: [
	    	    	fileList
	    	    ]
			});
		}
		win.show();    
		Ext.getCmp('query-result-file-list').store.load({
			params: {
				taskId: taskId
			}
		});   		
	};
	
	createQueryResultCmp = function(tabId) { 
		var explainField = {
			id: 'explain-' + tabId,
			name: 'explainField',
			region: 'center',
			autoScroll: true,
			border: false,
			xtype: 'textarea'
		};
		var resultGrid =  {
			xtype: 'gridpanel',
			name: 'resultGrid',
			autoScroll: true,
			id: 'result-grid' + tabId,
			region: 'center',
			layout:'fit',
	        forcefit: true,
	        border: false,
			store : Ext.create('Ext.data.Store', {
				fields : [],
	            proxy: {
				        type: 'ajax',
				        url : APPLICATION_CONTEXT+'hive/getQueryResult.do',
				        reader: {
				            type: 'json',
				            root: 'data'
				        },
						listeners: {
			                exception : Drone.exceptionListener
				        } 	
			    },
			    listeners: {
					load: function(store, meta){
						if(this.proxy.reader.jsonData.data == "error") {
							alert(this.proxy.reader.jsonData.msg);
							return;
						}
						var grid = Ext.getCmp(store.gridId);
						grid.reconfigure(store, store.proxy.reader.fields);
					}
				}
			}),
			viewConfig : {
				//disableSelection : true
			},
			columns : [],
	        tbar: [{
	        	text: 'Download result',
	        	iconCls:'save',
	        	//icon : APPLICATION_CONTEXT + 'resources/images/icons/fam/download.png',
	            handler: function() {

	        		//var tab = this.up('gridpanel').up('panel');
	            	var tab = Ext.getCmp('query-tab-' + tabId);
	            	if(!tab.taskId) return;
	            	showDownloadWindow(tab.taskId);
	            }
	        }, '->', {
	            text: 'Reload',
	            iconCls: 'x-tbar-loading',
	            handler: function() {
	        		var tab = Ext.getCmp('query-tab-' + tabId);
	            	if(!tab.taskId) return;
	                
	            	this.up('gridpanel').store.gridId = this.up('gridpanel').id;
	            	this.up('gridpanel').store.load({
						params : {
							taskId: tab.taskId
						}
					});
	            }
	        }]
		};

		return Ext.create('Ext.TabPanel', {
	        id : 'result-tab' + tabId,
			region: 'center', layout: 'fit', deferredRender: false, margins: '0 0 0 0',
	    	border : false, hideCollapseTool : true, activeTab: 0,
	    	split: true,
	        items: [{									    	
		        hideMode : Ext.isIE ? 'offsets' : 'display',
		    	layout: 'fit',
				title: 'Result',
		        items: [ resultGrid ],
		        doActivate: function(tab) {
	            },
		        listeners: {
		        	activate: function(tab) {
	                    tab.doActivate(tab);
	                }
	            }
	        }, {
		        title: 'Explain',
		        hideMode : Ext.isIE ? 'offsets' : 'display',
		    	layout: 'fit',
		        items: [ explainField ],
		        doActivate: function(tab) {
	            },
		        listeners: {
		        	activate: function(tab) {
	                    tab.doActivate(tab);
	                }
	            }		        
	        }]
		});
	};

	showSaveQueryWin = function(query, queryId, categoryName, queryName) {
		var queryValue = query == null ? '' : query;
		var win = Ext.getCmp('save-query-win');
		if(!win){
			var win = Ext.widget('window', {
	            title: 'Save Query', id: 'save-query-win', width: 650, height: 300, layout: 'border',
	            autoHeight: true, resizable: true, modal: true,
	            onEsc: function() { this.close(); },
	            items: [{ xtype:'form', id: 'save-query-form', region: 'center',
	        			frame: true, bodyPadding: 5, border :false, waitMsgTarget: true, defaultType: 'textfield',
	        	        fieldDefaults: { labelAlign: 'left', labelWidth: 100, anchor: '100%' },
	        	        items: [{ name: 'queryName', fieldLabel: 'Query Name', allowBlank: false, labelWidth: 100, value: queryName, anchor: '100%'},
	        	                { name: 'categoryName', xtype: 'combo', id:'save-query-form-category', triggerAction:  'all', forceSelection: true, editable: false, frame: true,
	        					  fieldLabel: 'Category', displayField: 'categoryName', valueField: 'categoryName', queryMode: 'local', allowBlank: false, anchor: '100%',
	        					  store: Ext.create('Ext.data.JsonStore', { fields : [ 'categoryName' ],
	        						  proxy : Ext.data.ScriptTagProxy({ 
	        							type : 'ajax',
	        							url : APPLICATION_CONTEXT + 'hive/listCategory.do'
	        						  })
	        					  })
	        	                },	
	        	     	        { xtype: 'textareafield', name: 'query', fieldLabel: 'query', labelAlign: 'top', flex: 1, margins: '0', allowBlank: false, anchor: '100%' , value: queryValue} ],
	        	     	buttons: [ { text: 'Close', handler: function() { this.up('window').destroy(); } },
	        	     		       { text: 'Add', disabled: true, formBind: true, 
	        			             handler: function() {
	        			            	if (!this.up('form').getForm().isValid()){
	        			            		return;
	        			            	}
	        			            	var serverJson = this.up('form').getForm().getFieldValues();
	        			            	var formPanel = this.up('form');
	        			            	var confirm = Ext.MessageBox.confirm('Are you sure?', 'Please confirm the saving of ' + serverJson['queryName'],
	        		            			function(btn){
	        	              					if (btn != 'yes') return;

	        	              					formPanel.getForm().submit({
	        	                        	    	clientValidation: true,
	        	                        	    	url: APPLICATION_CONTEXT + 'hive/saveQuery.do',
	        	                        	    	params: {
	        				            	        	params: Ext.encode(serverJson)
	        				            	    	},
	        	                        	    	success: function(form, action) {
	        	                        	    		formPanel.up('window').destroy();
	        	                        	    		Drone.msg(action.result.msg, '');
	        	                        	    	},
	        	                        	    	failure: function(form, action) {
	        	                        	    		Drone.onSuccessOrFailForm(form, action);
	        	                        	        	formPanel.up('window').destroy();
	        	                        	    	}
	        	                        		});	//form submit
	        		            		  }); //btn function
	        			             } //handler
	        			        }] //buttons
	            		}]//form
	        	    }); //create	    	            
		} else{
			win.center();
		}
		Ext.getCmp('save-query-form-category').store.load();
		win.show();
	}
	
	createExecuteQueryCmp = function(tabId){ 
		return {
			xtype: 'form', defaultType: 'textfield', frame: false, bodyBorder : true,
	        region: 'north', split: true,  height: 200, layout: 'border',
	        items: [{ xtype: 'checkbox', region:'north', height:20, margins:5, name: 'saveFile', labelSeparator: '', hideLabel: true, boxLabel: 'save result to hadoop', fieldLabel: 'save result to hadoop' },
	    	        { xtype: 'textarea', region:'center', id: 'query-textarea' + tabId, style: 'margin:0', hideLabel: true, allowBlank: false, name: 'query', anchor: '100%' }],
	    	buttons: [ {
	    		text: 'Save Query', iconCls:'save', disabled: true, formBind: true, width: 80,
	    		handler: function() {
	    			var form = this.up('form').getForm();
	    			showSaveQueryWin(form.findField('query').getValue());
	    		}
	    	}, {
		    	text: 'Explain', iconCls: 'settings', disabled: true, formBind: true, 
		    	handler: function() {
		    		if(window.connectionNode == undefined) {
			    		alert('select database');
			    		return;
		    		}
			    	var connectionName = window.connectionNode.get('connectionName');
					var tab = this.up('form').up('panel');
	        		var form = this.up('form').getForm();
	        		var explainField = tab.query('textarea[name="explainField"]')[0];
	        		if (!form.isValid()) {
	           			return;
	          		}
	        		Ext.Ajax.request({
	    		        url: APPLICATION_CONTEXT + 'hive/explain.do',
	    		        method: 'POST',
	    		        params: {
	    		        	connectionName : connectionName,
   							query: form.findField('query').getValue()
	    		        },
	    		        success: function(response, opts) {
	    		        	explainField.setValue(Ext.decode(response.responseText).data);
	    		        	Ext.getCmp('result-tab' + tabId).setActiveTab(1);
	    		        },
	    		        failure: function(response, opts) {
	    		        	Drone.onFailureAjax(response);
	    		        } 
	        		});	          		
	    		}          		
			}, {
		    	text: 'Execute', iconCls: 'accept', disabled: true, formBind: true, 
	            handler: function() {
		    		if(window.connectionNode == undefined) {
			    		alert('select database');
			    		return;
		    		}
					var connectionName = window.connectionNode.get('connectionName');
					var tab = this.up('form').up('panel');
					if(tab.runQuery == true) {
						alert('Query not completed');
						return; 
					}
	            	var form = this.up('form').getForm();
     				//var resultGrid = tab.query('gridpanel[title="Result"]')[0];
     				var resultGrid = Ext.getCmp('result-grid' + tabId);
          			resultGrid.store.loadData([]);
           	
              		if (!form.isValid()) {
               			return;
              		}
              		Ext.Ajax.request({
                      	url: APPLICATION_CONTEXT +'hive/executeQuery.do',
                       	params : {
       							connectionName : connectionName,
       							query: form.findField('query').getValue(),
       							saveFile: form.findField('saveFile').getValue()
       					},
        				success :function(response, opts) {
            				var taskId = Ext.decode(response.responseText).data;
            				tab.taskId = taskId;
            				Ext.TaskManager.start({
				            	run: function() {
	                    	 		var task = this;
	                				tab.task = task;
	                				tab.runQuery = true;
            		        		resultGrid.store.gridId = resultGrid.id;
            		        		resultGrid.store.load({
            							params : {
            								taskId : taskId
            							},
            							callback : function(records, operation, success){
            								if(!success) Ext.TaskManager.stop(task);
            								var response = Ext.decode(operation.response.responseText);
            								if(!response.retry){
            									Ext.TaskManager.stop(task);
                  								tab.runQuery = false;
                  							}
            							}
            						});
			                    },
			                    interval: 10000
			            	});
       					},
          				failure: function(response, opts) {
							tab.runQuery = false;
			            	Drone.onFailureAjax(response);
                  		}
                   });
               	}
	        }]
	    };
	};
	
	createQueryTabCmp = function(tabId){ 
		var name = 'Query-' + tabId;
		var id = 'query-tab-' + tabId 
		return {
			title: name,
			layout : 'border',
			id: id,
			autoHeight : true,
			border: false,
			split: 'true',
	        items: [createExecuteQueryCmp(tabId), createQueryResultCmp(tabId)],
	        doActivate: function(tab) {
            },
	        listeners: {
	        	activate: function(tab) {
                    tab.doActivate(tab);
                },
                close: function(tab) {
                }
            }
	    };
	};
	    
	createMultiQueryCmp = function(cmp_id){ 
		return {
			xtype: 'tabpanel',
			columnWidth : 1,
            id : cmp_id,
            enableTabScroll: true,
            activeTab: 0,
            layout: 'fit',
	        region: 'center',
	        split: true,
	        border: false,
            height: 600,
            tabNumber: 0,
            defaults: {
                autoScroll:true,
                closable: true
            },
            items: []
	    };
	};
	
    var viewport = Ext.create('Ext.Viewport', {
        layout: 'border',
        items: [{
        	cls:'x-panel-header-default x-panel-header-default-top titleBG',
        	border : false,
        	frame:true,
        	region: 'north',        	
        	margins: '0 5 5 5',
        	height: 45,
            dockedItems: [{
        	    xtype: 'toolbar',
        	    dock: 'top',
        	    style: {background: 'none'},
        	    items: [getTopMenuItems()]
        	}]
        },{
        	region: 'west',
            title : 'Hive Connection',
            iconCls : 'nav',
            split: true,
            width: 300,
            layout: 'fit',
            collapsible: true,
            animCollapse: true,
            margins: '0 0 0 5',
            items: [createLeftCmp()]
        },{
        	region: 'center',
            split: true,
            margins: '0 0 0 0',
            autoScroll: true,
            layout : 'border',
            anchor: '100%',
            items: [ createMultiQueryCmp('query-tab-cmp') ],
            tbar:[{
	        	text   : 'Add Query',
	        	iconCls : 'add',
	            handler: function(btn, event) {
	            	var tabs = Ext.getCmp('query-tab-cmp');
	            	tabs.tabNumber++;
	            	tabs.add(createQueryTabCmp(tabs.tabNumber)).show();
	            }
	        }, '->', {
	        	text   : 'Query history',
	        	iconCls : 'info',
	            handler: function(btn, event) {
	            	showHistoryWin()
	            }
	        }]            
        },{
        	region: 'east',
            title : 'Query',
            iconCls : 'nav',
            split: true,
            width: 250,
            layout: 'fit',
            collapsible: true,
            animCollapse: true,
            margins: '0 5 0 0',
            items: [createRightCmp()]
        }]
    });
});

Ext.onReady(function() {
	var treePanel = Ext.getCmp('hive-connection-tree-cmp');

	treePanel.getSelectionModel().on('select', function(selModel, record) {
		if (record.get('leaf') && record.get('category') == 'table') {
			var grid = Ext.getCmp('column-grid-cmp');
			grid.store.load({
				params : {
					connectionName : record.get('connectionName'),
					tableName : record.get('value')
				}
			});
		} else {
			if (record.get('category') == 'connection') {
				window.connectionNode = record;
			
				var form = Ext.getCmp('hiveConnection-detail-form').getForm();
				
				form.findField("connectionName").setValue(record.get('connectionName'));
				form.findField("driverClass").setValue(record.get('hiveConn').driverClass);
				form.findField("connectionUri").setValue(record.get('hiveConn').connectionUri);
				form.findField("fileSystemUri").setValue(record.get('hiveConn').fileSystemUri);
				form.findField("description").setValue(record.get('hiveConn').description);
			}
						
			record.expand();
		}
	});

	var tabs = Ext.getCmp('query-tab-cmp');
	tabs.tabNumber++;
	tabs.add(createQueryTabCmp(tabs.tabNumber)).show();

	var downFrame = Ext.getBody().createChild({
        tag:'iframe', 
       	cls:'x-hidden', 
       	id:'hive-download-iframe',
       	name:'hive-download-iframe'
   	});

	downloadForm = new Ext.FormPanel ({
		url: APPLICATION_CONTEXT + 'hive/downloadResult.do',
		cls:'x-hidden',
		id:'hive-download-form',
		standardSubmit: true,
		items: [{
			            xtype: 'textfield',
			            name: 'taskId'
			        }, {
			            xtype: 'textfield',
			            name: 'path'
			        }
			    ]
	});

	downloadForm.render(Ext.getBody());
	
});

</script>
</head>
<body>
</body>
</html>