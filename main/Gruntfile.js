module.exports = function(grunt) {

  // ===========================================================================
  // CONFIGURE GRUNT ===========================================================
  // ===========================================================================
  grunt.initConfig({

    // get the configuration info from package.json
    pkg: grunt.file.readJSON('package.json'),

    // all of our configuration will go here
    jshint: {
    	main : {
    	      options: {
    	          reporter: require('jshint-stylish') // use jshint-stylish to make our errors look and read good
    	        },

    	          // when this task is run, lint the Gruntfile and all js files in src
    	        build: ['Grunfile.js', ['src/main/webapp/resources/js/anuglar-controllers/*.js', 
    	                                'src/main/webapp/resources/js/main.js',
    	                                'src/main/webapp/resources/js/angular-general-functions.js',
    	                                'src/main/webapp/resources/js/angular-common.js']]
    	 },
    	 
    	 admin : {
    	      options: {
    	          reporter: require('jshint-stylish') // use jshint-stylish to make our errors look and read good
    	        },

    	          // when this task is run, lint the Gruntfile and all js files in src
    	        build: ['Grunfile.js', ['src/main/webapp/resources/js/admin/inv/ctrlrs/*.js', 
    	      	  					  'src/main/webapp/resources/js/admin/inv/othr/*.js']]
    	 }
    },
      
    // configure uglify to minify js files -------------------------------------    
    uglify: {  
        main:{  
            options:{  
                banner:'/*<%= grunt.template.today("yyyy-mm-dd h:MM TT") %>*/\n',
                mangle:false,
                sourceMap:true
            },
            files:{  
                'src/main/webapp/resources/js/comb.min.js':[  
                    'src/main/webapp/resources/js/anuglar-controllers/*.js',
                    'src/main/webapp/resources/js/main.js',
                    'src/main/webapp/resources/js/angular-general-functions.js',
                    'src/main/webapp/resources/js/angular-common.js'
                ]
            }
        },
        admin:{  
            options:{  
                banner:'/*<%= grunt.template.today("yyyy-mm-dd h:MM TT") %>*/\n',
                mangle:false,
                sourceMap:true
            },
            files:{  
                'src/main/webapp/resources/js/admin/inv/admin-comb.min.js':[  
                    'src/main/webapp/resources/js/admin/inv/othr/*.js',
                    'src/main/webapp/resources/js/admin/inv/ctrlrs/*.js',
                    'src/main/webapp/resources/js/admin/inv/main.js'
                ]
            }
        }
    }, 
    
    concat: {  
        dist:{  
            src:'src/main/webapp/resources/js/lib/cdn/*.js',
            dest:'src/main/webapp/resources/js/lib/cdn.min.js',

        }
    },
    
    // configure watch to auto update ----------------
    watch: {  
        scripts:{  
            files:[  
                'src/main/webapp/resources/js/anuglar-controllers/*.js',
                'src/main/webapp/resources/js/main.js',
                'src/main/webapp/resources/js/angular-general-functions.js',
                'src/main/webapp/resources/js/angular-common.js'
            ],
            tasks:[  
                'jshint:main',
                'uglify:main'
            ]
        },
        scriptsadmin:{  
            files:[  
                'src/main/webapp/resources/js/admin/inv/othr/*.js',
                'src/main/webapp/resources/js/admin/inv/ctrlrs/*.js',
                'src/main/webapp/resources/js/admin/inv/main.js'
            ],
            tasks:[  
                'jshint:admin',
                'uglify:admin'
            ]
        }
    }
  });

  // ===========================================================================
  // LOAD GRUNT PLUGINS ========================================================
  // ===========================================================================
  // we can only load these if they are in our package.json
  // make sure you have run npm install so our app can find these
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-watch');
  
  
  //============= // CREATE TASKS ========== //
  grunt.registerTask('default', ['jshint', 'uglify']);

};