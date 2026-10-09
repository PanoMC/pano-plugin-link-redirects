// Sample data of RedirectPage for the view catalogue (doc 02 section 7). Pure data: import only view helpers and
// relative .js fixtures. The delay is long so the catalogue page is not left while it is looked at.
/** @type {string[]} */
export const notApplicable = ['error', 'loading'];

/** @type {import('@panomc/plugin-kit').Samples} */
export default {
  filled: {
    props: {
      data: {
        redirect: {
          targetUrl: 'https://example.com/discord',
          delay: 3600,
          showIntermediatePage: true,
          intermediatePageDesign: 'MODERN',
          useCustomPage: false,
          htmlContent: '',
          openInNewTab: false,
        },
      },
    },
  },
  empty: {
    props: { data: { redirect: null } },
  },
};
