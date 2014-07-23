describe('Module: Localisation', function() {

  var $window, LocalisationService, UserInfo, $compile,
      key = 'Test.avain',
      value = 'Testataan lokalisaatioiden toimivuus',
      locale = 'se',
      id = 1,
      mockLocalisations = [{key: key, value: value, locale: locale, id: id}];

  beforeEach(function() {
    UserInfo = { lang: 'se' };
    $window = {APP_LOCALISATION_DATA: mockLocalisations};

    module(function($provide) {
      $provide.value('UserInfo', UserInfo);
      $provide.value('$window', $window);
    });

    module('localisation');

    inject(function($injector) {
      LocalisationService = $injector.get('LocalisationService');
      $compile = $injector.get('$compile');
    });
  });

  /*
   * Please note: Only the aspects of LocalisationService that are currently used are tested
   */
  describe('Service: LocalisationService', function() {

    beforeEach(function() {
    });

    it('initializes properly', function() {
      expect(LocalisationService.locale).toEqual(locale);

      // updateLookupMap is called on initialization and works like it should
      expect(angular.isObject( LocalisationService.localisationMapByLocaleAndKey) ).toBeTruthy();
      expect(angular.isObject( LocalisationService.localisationMapByLocaleAndKey[locale] )).toBeTruthy();
      expect(angular.isObject( LocalisationService.localisationMapByLocaleAndKey[locale][key]) ).toBeTruthy();
      expect( LocalisationService.localisationMapByLocaleAndKey[locale][key] ).toEqual(mockLocalisations[0]);
    });

    it('getLocale() returns the correct locale when locale is set', function() {
      expect(LocalisationService.getLocale()).toEqual(locale);
    });

    it('setLocale() changes the current locale', function() {
      LocalisationService.setLocale('en');
      expect(LocalisationService.getLocale()).toEqual('en');
    });

    it('getLocale() returns and sets the current locale to \'fi\' when locale is undefined', function() {
      LocalisationService.setLocale(undefined);
      expect(LocalisationService.getLocale()).toEqual('fi');
      expect(LocalisationService.locale).toEqual('fi');
    });

    it('getRawTranslation() returns the translation if it exists and flags its id to have been accessed', function() {
      var translation = LocalisationService.getRawTranslation(key, locale);
      expect(translation).toEqual(value);

      expect(LocalisationService.updateAccessedById[id]).toBeDefined();
    });

    it('getRawTranslation() throws an Error if the key is not a String', function() {
      var faultyKey = ['that', 'is', 'one', 'faulty', 'key', '!', 111];
      function faultyKeyCall() {
        return LocalisationService.getRawTranslation(faultyKey, locale);
      }
      expect(faultyKeyCall).toThrow(new Error("Illegal translation key: '"+faultyKey+"'"));
    });

    it('getRawTranslation() uses getLocale if locale parameter is not a String', function() {
      spyOn(LocalisationService, 'getLocale').andCallThrough();
      var translation = LocalisationService.getRawTranslation(key, ['not', 'a', 'string']);

      expect(translation).toEqual(value);
      expect(LocalisationService.getLocale.callCount).toBe(1);
    });

    it('getRawTranslation() returns undefined if the given locale or key isn\'t found and flags nothing to have been accessed', function() {
      expect( LocalisationService.getRawTranslation(key, 'en') ).toBeUndefined();
      expect( LocalisationService.getRawTranslation('Does.not.exist', locale) ).toBeUndefined();

      expect(angular.toJson( LocalisationService.updateAccessedById )).toEqual('{}');
    });

    it('getTranslation() returns the translation if it can be found using getRawTranslation', function() {
      spyOn(LocalisationService, 'getRawTranslation').andCallThrough();
      var translation = LocalisationService.getTranslation(key, locale);

      expect(translation).toEqual(value);
      expect(LocalisationService.getRawTranslation.callCount).toBe(1);
    });

    it('getTranslation() returns an error message if the translation is not found', function() {
      expect( LocalisationService.getTranslation(key, 'en') )
        .toEqual('Missing translation ' + key + ' for locale en');

      expect( LocalisationService.getTranslation('Does.not.exist', locale) )
        .toEqual('Missing translation Does.not.exist for locale ' + locale);
    });

    it('getTranslation() injects parameters into the translation if they are provided', function() {
      var translationWithParameters = 'This translations has not one ({0}) but two ({1}) parameters.'
      LocalisationService.localisationMapByLocaleAndKey['se']['Has.parameters'] = { value: translationWithParameters };

      var param1 = '**first param**',
          param2 = '**second param**',
          translation = LocalisationService.getTranslation('Has.parameters', 'se', [param1, param2]);

      expect(translation)
        .toEqual('This translations has not one ('+ param1 +') but two ('+ param2 +') parameters.');
    });

    it('getTranslation() a lone parameter can be inserted without array', function() {
      var translationWithParameters = 'This translations has just one ({0}) parameter.'
      LocalisationService.localisationMapByLocaleAndKey['se']['Has.parameter'] = { value: translationWithParameters };

      var param1 = '**first param**',
          translation = LocalisationService.getTranslation('Has.parameter', 'se', param1);

      expect(translation)
        .toEqual('This translations has just one ('+ param1 +') parameter.');
    });

    it('hasTranslation() returns true if translation exist and false if not', function() {
      expect(LocalisationService.hasTranslation(key, locale)).toBeTruthy();
      expect(LocalisationService.hasTranslation(key, 'en')).toBeFalsy();
    });

  });
  
  describe('Directive: tt', function() {
    var elem, compileTemplate, scope;

    beforeEach(function() {
      scope = {};
      compileTemplate = function(template) {
        elem = $compile(template)(scope);
      };
    });

    afterEach(function() {
      elem.remove();
    });

    it('inserts the translation defined by the value of tt and the current locale by default', function() {
      compileTemplate('<span tt="'+ key +'"></span>');
      expect(elem.text()).toBe(value);
    });

    it('inserts the translation defined by the value of tt and the defined locale attribute', function() {
      LocalisationService.localisationMapByLocaleAndKey['fi'] = {};
      LocalisationService.localisationMapByLocaleAndKey['fi'][key] = {value: 'Jotain muuta'};

      compileTemplate('<span tt="'+ key +'" locale="fi"></span>');
      expect(elem.text()).toBe('Jotain muuta');
    });
  });
});
