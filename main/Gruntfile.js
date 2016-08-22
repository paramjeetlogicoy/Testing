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
      build: ['Grunfile.js', 'src/main/webapp/resources/js/*.js']
    },
      
    // configure uglify to minify js files -------------------------------------
    uglify: {
      options: {
        banner: '/*\n <%= pkg.name %> <%= grunt.template.today("yyyy-mm-dd") %> \n*/\n'
      },
      
      build: {
        files: {
          'src/main/webapp/resources/js/main.min.js': 'src/main/webapp/resources/js/*.js'
        }
      }
    },
      
    less: {
      build: {
        files: {
          'src/main/webapp/resources/css/main.css': 'src/main/webapp/resources/css/less/main.less'
        }
      }
    },
    
    // configure cssmin to minify css files ------------------------------------
    cssmin: {
      options: {
        banner: '/*\n <%= pkg.name %> <%= grunt.template.today("yyyy-mm-dd") %> \n*/\n'
      },
      build: {
        files: {
          'src/main/webapp/resources/css/main.min.css': 'src/main/webapp/resources/css/main.css'
        }
      }
    },
    
    // configure watch to auto update ----------------
    watch: {
      
      // for stylesheets, watch css and less files 
      // only run less and cssmin stylesheets: 
      css : {
    	files: ['src/main/webapp/resources/css/less/*.less'], 
    	tasks: ['less', 'cssmin']
      },

      // for scripts, run jshint and uglify 
      scripts: { 
        files: 'src/main/webapp/resources/js/*.js', tasks: ['jshint', 'uglify'] 
      } 
    }
  });

  // ===========================================================================
  // LOAD GRUNT PLUGINS ========================================================
  // ===========================================================================
  // we can only load these if they are in our package.json
  // make sure you have run npm install so our app can find these
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-contrib-watch');
  
  
  //============= // CREATE TASKS ========== //
  grunt.registerTask('default', ['jshint', 'uglify', 'less', 'cssmin']);

};