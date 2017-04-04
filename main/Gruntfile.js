module.exports = function(grunt) {

  // ===========================================================================
  // CONFIGURE GRUNT ===========================================================
  // ===========================================================================
  grunt.initConfig({

    // get the configuration info from package.json
    pkg: grunt.file.readJSON('package.json'),

    // all of our configuration will go here
    jshint: {
      options: {
        reporter: require('jshint-stylish') // use jshint-stylish to make our errors look and read good
      },

        // when this task is run, lint the Gruntfile and all js files in src
      build: ['Grunfile.js', ['src/main/webapp/resources/js/anuglar-controllers/*.js', 
                              'src/main/webapp/resources/js/main.js',
                              'src/main/webapp/resources/js/angular-general-functions.js',
                              'src/main/webapp/resources/js/angular-common.js']]
    },
      
    // configure uglify to minify js files -------------------------------------    
    uglify: {
        options: {
          banner: '/*<%= grunt.template.today("yyyy-mm-dd h:MM TT") %>*/\n',
          mangle: false,
          //beautify : true,
          sourceMap: true
        },
        build: {
          files: {
            'src/main/webapp/resources/js/comb.min.js': ['src/main/webapp/resources/js/anuglar-controllers/*.js', 
                                                         'src/main/webapp/resources/js/main.js',
                                                         'src/main/webapp/resources/js/angular-general-functions.js',
                                                         'src/main/webapp/resources/js/angular-common.js']
          }
       }
    }, 
    
    less: {
        options: {
            banner: '/*<%= grunt.template.today("yyyy-mm-dd h:MM TT") %>*/\n'
        },
        main: {
            files: {
                'src/main/webapp/resources/css/main.css': 'src/main/webapp/resources/css/less/main.less'
            }
        },
        admin: {
            files: {
                'src/main/webapp/resources/css/admin.css': 'src/main/webapp/resources/css/less/admin.less'
            }
        }
    },
      
	cssmin: {
        options: {
            banner: '/*<%= grunt.template.today("yyyy-mm-dd h:MM TT") %>*/\n',
            selectorsSortingMethod: 'none'
        },
        main: {
            files: {
                'src/main/webapp/resources/css/main.min.css': 'src/main/webapp/resources/css/main.css'
            }
        },
        admin: {
            files: {
                'src/main/webapp/resources/css/admin.min.css': 'src/main/webapp/resources/css/admin.css'
            }
        }
	},
    
    concat: {
      dist: {
        src: 'src/main/webapp/resources/js/lib/cdn/*.js',
        dest: 'src/main/webapp/resources/js/lib/cdn.min.js',
      }
    },
    
    // configure watch to auto update ----------------
    watch: {
        lessmain : {
            files: ['src/main/webapp/resources/css/less/overrides.less',
            	'src/main/webapp/resources/css/less/typesetting.less',
            	'src/main/webapp/resources/css/less/main.less'],
            tasks: ['less:main', 'cssmin:main'],
            options: {
               spawn: false
            }
    	},
        	
    	lessadmin : {
            files: ['src/main/webapp/resources/css/less/overrides.less',
            	'src/main/webapp/resources/css/less/typesetting.less',
            	'src/main/webapp/resources/css/less/admin.less'],
            tasks: ['less:admin', 'cssmin:admin'],
           options: {
              spawn: false
           }
        },
        
      // for scripts, run jshint and uglify 
      scripts: { 
        files: ['src/main/webapp/resources/js/anuglar-controllers/*.js', 
                'src/main/webapp/resources/js/main.js',
                'src/main/webapp/resources/js/angular-general-functions.js',
                'src/main/webapp/resources/js/angular-common.js'], 
        tasks: ['jshint', 'uglify'] 
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
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  
  
  //============= // CREATE TASKS ========== //
  grunt.registerTask('default', ['less', 'cssmin', 'jshint', 'uglify']);

};