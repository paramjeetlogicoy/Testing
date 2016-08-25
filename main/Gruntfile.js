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
          banner: '/*<%= grunt.template.today("yyyy-mm-dd") %>*/\n',
          mangle: false,
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
    
    concat: {
      dist: {
        src: 'src/main/webapp/resources/js/lib/cdn/*.js',
        dest: 'src/main/webapp/resources/js/lib/cdn.min.js',
      }
    },
    
    // configure watch to auto update ----------------
    watch: {
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
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-watch');
  
  
  //============= // CREATE TASKS ========== //
  grunt.registerTask('default', ['jshint', 'uglify']);

};