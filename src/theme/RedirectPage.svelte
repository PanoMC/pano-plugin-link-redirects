<div
  class="redirects-redirect-page redirects-redirect-page"
  class:is-custom-content={redirect?.useCustomPage}
  class:is-instant={isInstantRedirectUi(redirect)}
  style={redirect?.useCustomPage ? 'display: block;' : ''}>
    {#if redirect && redirect.showIntermediatePage}
        <div class="redirects-redirect-page__overlay" class:is-custom-content={redirect?.useCustomPage}>
            {#if redirect.intermediatePageDesign === 'MINIMAL'}
                <div class="redirects-redirect-page__loader" style="border-width: 3px; width: 32px; height: 32px; margin: 0 1rem 0 0; border-top-color: white; border-right-color: transparent;"></div>
                <div style="font-size: 1.1rem; font-weight: 500;">
                    {$_('pages.redirects.theme.redirecting-to-in', { values: { hostname, seconds: remaining } })}
                </div>
            {:else if redirect.intermediatePageDesign === 'MODERN'}
                <div class="redirects-redirect-page__card" style="background: #1f2937; color: white;">
                    <div class="redirects-redirect-page__loader" style="border-top-color: #60a5fa; border-right-color: transparent;"></div>
                    <div class="redirects-redirect-page__title" style="color: white;">{$_('pages.redirects.theme.redirecting')}</div>
                    <div class="redirects-redirect-page__url" style="color: #9ca3af;">{$_('pages.redirects.theme.taking-you-to', { values: { hostname } })}</div>
                    <div class="redirects-redirect-page__progress" style="background: #374151;">
                        <div id="pano-redirect-progress" class="redirects-redirect-page__progress-bar" style="background: #60a5fa; width: {progress}%"></div>
                    </div>
                    <div style="margin-top: 0.5rem; font-size: 0.8rem; color: #6b7280;">{$_('pages.redirects.theme.please-wait')}</div>
                </div>
            {:else if redirect.intermediatePageDesign === 'CUSTOM'}
                <!-- Custom design: We don't render standard cards. 
                     The overlay will remain if showIntermediatePage is true, 
                     but it will be transparent/blurred according to .is-custom-content styles -->
            {:else}
                <div class="redirects-redirect-page__card">
                    <div class="redirects-redirect-page__loader"></div>
                    <div class="redirects-redirect-page__title">{$_('pages.redirects.theme.redirecting')}...</div>
                    <div class="redirects-redirect-page__url">{redirect.targetUrl}</div>
                    <div class="mt-3">
                        {#if remaining > 0}
                            {$_('pages.redirects.theme.redirect-in', { values: { seconds: remaining } })}
                        {:else}
                            {$_('pages.redirects.theme.redirecting-now')}
                        {/if}
                    </div>
                </div>
            {/if}
        </div>
    {/if}
    
    {#if redirect?.useCustomPage && redirect?.htmlContent}
        <div class="redirects-redirect-page__custom-content">
            {@html redirect.htmlContent}
        </div>
    {/if}
</div>

<style>
  /* Kept rules: every rule draws the plugin's own redirect page (page, overlay, card, loader, progress bar, the two
     animations); no selector reaches Bootstrap or theme markup. */
  .redirects-redirect-page {
    min-height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  /* Full black only when showing intermediate overlay or a delayed non-intermediate wait (legacy). */
  .redirects-redirect-page:not(.is-custom-content):not(.is-instant) {
    background: #000;
  }

  .redirects-redirect-page__overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.9);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 999999;
    color: white;
    font-family: system-ui, -apple-system, sans-serif;
    backdrop-filter: blur(5px);
  }

  .redirects-redirect-page__overlay.is-custom-content {
    background: transparent;
    backdrop-filter: none;
    z-index: 10;
  }

  .redirects-redirect-page__custom-content {
    position: relative;
    z-index: 5;
    width: 100%;
    min-height: 100vh;
    color: white;
  }
  .redirects-redirect-page__card {
    background: white;
    color: #333;
    padding: 2.5rem;
    border-radius: 16px;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
    text-align: center;
    max-width: 400px;
    width: 90%;
    animation: redirects-scale-in 0.3s ease-out;
  }
  .redirects-redirect-page__title {
    font-size: 1.5rem;
    margin-bottom: 0.5rem;
    font-weight: 700;
    color: #111;
  }
  .redirects-redirect-page__url {
    font-size: 0.9rem;
    color: #666;
    margin-bottom: 1.5rem;
    word-break: break-all;
  }
  .redirects-redirect-page__loader {
    width: 48px;
    height: 48px;
    border: 4px solid #f3f3f3;
    border-top: 4px solid #3b82f6;
    border-radius: 50%;
    animation: redirects-spin 1s linear infinite;
    margin: 0 auto 1.5rem;
  }
  .redirects-redirect-page__progress {
    height: 6px;
    background: #e5e7eb;
    border-radius: 3px;
    overflow: hidden;
    margin-top: 1.5rem;
    width: 100%;
  }
  .redirects-redirect-page__progress-bar {
    height: 100%;
    background: #3b82f6;
    transition: width 1s linear;
  }
  @keyframes redirects-spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
  @keyframes redirects-scale-in {
    0% { transform: scale(0.9); opacity: 0; }
    100% { transform: scale(1); opacity: 1; }
  }
</style>

<script context="module">
  import { api } from '@panomc/sdk/plugin-api';
  import { error, redirect as svelteRedirect } from '@panomc/sdk/svelte';

  /** API / stores may hand back non-boolean flags; `"false"` is truthy in JS and would wrongly show intermediate UI. */
  function coerceBool(v) {
    if (v === true || v === 1) return true;
    if (v === false || v === 0) return false;
    if (v == null || v === '') return false;
    if (typeof v === 'string') {
      const s = v.trim().toLowerCase();
      return s === 'true' || s === '1' || s === 'yes';
    }
    return false;
  }

  function normalizeRedirectPayload(res) {
    if (!res || typeof res !== 'object') return res;
    return {
      ...res,
      showIntermediatePage: coerceBool(res.showIntermediatePage),
      useCustomPage: coerceBool(res.useCustomPage),
      openInNewTab: coerceBool(res.openInNewTab),
    };
  }

  export async function load(event) {
    const currentPath = event.url.pathname;

    const res = await api.get({
      path: `/link-redirects/check?path=${currentPath}`,
      request: event
    });

    if (!res || res.error || (res.status && res.status !== 'SUCCESS')) {
      if (res && (res.error?.code === 'NOT_LOGGED_IN')) {
        throw svelteRedirect(302, `/login?redirect=${encodeURIComponent(currentPath)}`);
      }
      return error(404, 'Redirect not found or access denied');
    }

    const redirect = normalizeRedirectPayload(res);

    // Instant HTTP redirect when there is no page to render. openInNewTab only sets nav target=_blank
    // (redirect path opens in a new tab); SSR still responds with 302 so that tab follows to the final URL.
    if (
      (Number(redirect.delay) || 0) <= 0 &&
      !redirect.showIntermediatePage &&
      !redirect.useCustomPage
    ) {
      throw svelteRedirect(302, redirect.targetUrl);
    }

    return { data: { redirect } };
  }
</script>

<script>
  import { onMount } from 'svelte';
  import { derived } from 'svelte/store';
  import { _ as i18n } from '@panomc/sdk/utils/language';

  // plugin translations: `$_('key')` reads `plugins.pano-plugin-link-redirects.key`
  const _ = derived(i18n, ($_fn) => (key, options) => $_fn(`plugins.pano-plugin-link-redirects.${key}`, options));
  export let data;
  
  const redirect = data?.redirect;

  /** No intermediate/custom chrome; only a quick client redirect (avoid empty black fullscreen). */
  function isInstantRedirectUi(r) {
    if (!r) return false;
    const delay = Number(r.delay) || 0;
    return delay <= 0 && !r.showIntermediatePage && !r.useCustomPage;
  }

  let remaining = Number(redirect?.delay) || 0;
  let progress = 0;
  let hostname = '';

  try {
    hostname = new URL(redirect?.targetUrl).hostname;
  } catch (e) {
    hostname = redirect?.targetUrl || '';
  }

  onMount(() => {
    if (!redirect) return;
    
    if (remaining <= 0) {
      performRedirect();
      return;
    }

    const interval = setInterval(() => {
      remaining--;
      progress = ((redirect.delay - remaining) / redirect.delay) * 100;
      if (remaining <= 0) {
        clearInterval(interval);
        performRedirect();
      }
    }, 1000);

    return () => clearInterval(interval);
  });

  function performRedirect() {
    if (!redirect) return;
    // Same-tab navigation: "open in new tab" applies only to nav links (target _blank on the redirect path).
    window.location.href = redirect.targetUrl;
  }
</script>
