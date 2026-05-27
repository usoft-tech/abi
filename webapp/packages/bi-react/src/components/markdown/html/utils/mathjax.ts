export {};

declare global {
  interface Window {
    MathJax: any;
  }
}

window.MathJax = {
  tex: {
    packages: ['base'], // extensions to use
    inlineMath: [
      // start/end delimiter pairs for in-line math
      ['$', '$'],
      ['\\(', '\\)'],
    ],
    displayMath: [
      // start/end delimiter pairs for display math
      ['$$', '$$'],
      ['\\[', '\\]'],
    ],
    processEscapes: true, // use \$ to produce a literal dollar sign
    processEnvironments: true, // process \begin{xxx}...\end{xxx} outside math mode
    processRefs: true, // process \ref{...} outside of math mode
    digits: /^(?:[0-9]+(?:\{,\}[0-9]{3})*(?:\.[0-9]*)?|\.[0-9]+)/,
    // pattern for recognizing numbers
    tags: 'none', // or 'ams' or 'all'
    tagSide: 'right', // side for \tag macros
    tagIndent: '0.8em', // amount to indent tags
    useLabelIds: true, // use label name rather than tag for ids
    maxMacros: 10000, // maximum number of macro substitutions per expression
    maxBuffer: 5 * 1024, // maximum size for the internal TeX string (5K)
    baseURL: // URL for use with links to tags (when there is a <base> tag in effect)
      document.getElementsByTagName('base').length === 0
        ? ''
        : String(document.location).replace(/#.*$/, ''),
    formatError:
      // function called when TeX syntax errors occur
      (jax: any, err: any) => jax.formatError(err),
  },
  options: {
    skipHtmlTags: [
      //  HTML tags that won't be searched for math
      'script',
      'noscript',
      'style',
      'textarea',
      'pre',
      'code',
      'annotation',
      'annotation-xml',
    ],
    includeHtmlTags: {
      //  HTML tags that can appear within math
      br: '\n',
      wbr: '',
      '#comment': '',
    },
    ignoreHtmlClass: 'tex2jax_ignore', //  class that marks tags not to search
    processHtmlClass: 'tex2jax_process', //  class that marks tags that should be searched
    compileError: function (doc: any, math: any, err: any) {
      doc.compileError(math, err);
    },
    typesetError: function (doc: any, math: any, err: any) {
      doc.typesetError(math, err);
    },
    // renderActions: {...}
  },
  startup: {
    elements: null, // The elements to typeset (default is document body)
    typeset: true, // Perform initial typeset?
    // ready: Startup.defaultReady.bind(Startup),          // Called when components are loaded
    // pageReady: Startup.defaultPageReady.bind(Startup),  // Called when MathJax and page are ready
    document: document, // The document (or fragment or string) to work in
    invalidOption: 'warn', // Are invalid options fatal or produce an error?
    // optionError: OPTIONS.optionError,  // Function used to report invalid options
    input: [], // The names of the input jax to use from among those loaded
    output: null, // The name for the output jax to use from among those loaded
    handler: null, // The name of the handler to register from among those loaded
    adaptor: null, // The name for the DOM adaptor to use from among those loaded
  },
  svg: {
    scale: 1, // global scaling factor for all expressions
    minScale: 0.5, // smallest scaling factor to use
    mtextInheritFont: false, // true to make mtext elements use surrounding font
    merrorInheritFont: true, // true to make merror text use surrounding font
    mathmlSpacing: false, // true for MathML spacing rules, false for TeX rules
    skipAttributes: {}, // RFDa and other attributes NOT to copy to the output
    exFactor: 0.5, // default size of ex in em units
    displayAlign: 'center', // default for indentalign when set to 'auto'
    displayIndent: '0', // default for indentshift when set to 'auto'
    fontCache: 'local', // or 'global' or 'none'
    localID: null, // ID to use for local font cache (for single equation processing)
    internalSpeechTitles: true, // insert <title> tags with speech content
    titleID: 0, // initial id number to use for aria-labeledby titles
  },
};
