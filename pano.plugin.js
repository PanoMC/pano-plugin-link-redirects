// Plugin-level options of the Pano plugin kit (@panomc/plugin-kit). The namespace is `redirects`, not the default
// `link-redirects`: a root class that starts with `link-` is a Bootstrap class. The only view file sits in src/theme.
export default {
  namespace: 'redirects',
  viewDirs: ['src/theme'],
  styles: {
    // RedirectPage draws the MODERN and MINIMAL designs with inline colours and sizes and a progress bar whose width is the
    // elapsed time; inline values keep winning over a theme's unlayered rules, which a layered plugin.css class would not.
    styleAttrAllow: ['RedirectPage'],
  },
};
