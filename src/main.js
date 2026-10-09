import {PanoPlugin, viewComponent} from '@panomc/sdk';
import { derived } from 'svelte/store';
import { api } from '@panomc/sdk/plugin-api';

const pluginId = 'pano-plugin-link-redirects';

// this is to render plugin translations
export const _ = derived(i18n, ($_fn) => {
  return (key, options) => $_fn(`plugins.${pluginId}.${key}`, options);
});

import { _ as i18n } from '@panomc/sdk/utils/language';
import { showToast } from '@panomc/sdk/toasts';

// Success/failure colouring for this plugin's toasts, matching the panel. showToast from
// @panomc/sdk/toasts is the host panel's ToastContainer `show`, whose signature is
// (text, params, toastComponent, options): passing undefined for toastComponent keeps the
// host's DefaultToast, and options.variant maps to Bootstrap's text-success / text-danger.
// These live here rather than in @panomc/sdk/toasts because this plugin is pinned to
// @panomc/sdk 1.0.0-dev.39, which predates the variants; they can be dropped for a direct
// SDK import once that pin moves. On an older panel build the extra argument is ignored and
// the toast renders neutral, so this degrades instead of breaking.
export function showSuccessToast(text, params = {}) {
  return showToast(text, params, undefined, { variant: 'success' });
}

export function showErrorToast(text, params = {}) {
  return showToast(text, params, undefined, { variant: 'danger' });
}


export default class PanoLinkRedirectsPlugin extends PanoPlugin {
  async onLoad() {
    const pano = this.pano;

    if (pano.isPanel) {
      // Register Panel Page
      pano.ui.page.register({
        path: '/link-redirects',
        component: viewComponent(() => import('./panel/LinkRedirectsPage.svelte')),
        permission: `pano.plugin.${pluginId}.manage.redirects`,
      });

      // Add to Sidebar
      pano.ui.nav.site.editNavLinks((navigationItems) => {
        const redirectLink = {
          href: '/link-redirects',
          icon: 'fas fa-link',
          text: `plugins.${pluginId}.pages.redirects.title`,
          startsWith: false,
          permission: `pano.plugin.${pluginId}.manage.redirects`,
        };

        const postIndex = navigationItems.findIndex((item) => item.href === '/posts');
        if (postIndex !== -1) {
          navigationItems.splice(postIndex + 1, 0, redirectLink);
        } else {
          navigationItems.push(redirectLink);
        }

        return navigationItems;
      });
    } else {
      // Theme Redirection Logic
      // SSR runs in a long-lived Node process where `registeredPages` and `siteNavLinks` are
      // module-level state. `theme:app:load` fires on every request, but plugin re-init is
      // cached by plugin-version hash — so without an explicit unregister step, entries for
      // deleted/renamed redirects would linger until the process restarts (SSR sees stale
      // routes/links, CSR sees fresh state → "ghost during SSR, gone after hydration").
      const registeredPaths = new Set();
      const navAddedHrefs = new Set();

      pano.ui.app.onLoad(async (data, event) => {
        // Fetch active redirects to register their routes
        try {
          const res = await api.get({
            path: '/link-redirects',
            request: event
          });

          if (!res || !Array.isArray(res.items)) return;

          const incomingPaths = new Set(res.items.map((r) => r.path));
          const incomingNavHrefs = new Set(
            res.items.filter((r) => r.showInNavigation).map((r) => r.path)
          );

          // Drop route entries we previously registered that are no longer in the response.
          for (const oldPath of registeredPaths) {
            if (!incomingPaths.has(oldPath)) {
              pano.ui.page.unregister(oldPath);
              registeredPaths.delete(oldPath);
            }
          }

          // Drop nav links we previously added that should no longer be present (deleted
          // redirect or showInNavigation toggled off). Only touch hrefs we ourselves added,
          // so we don't clobber links owned by other plugins.
          const navHrefsToRemove = [];
          for (const href of navAddedHrefs) {
            if (!incomingNavHrefs.has(href)) navHrefsToRemove.push(href);
          }
          if (navHrefsToRemove.length && pano.ui.nav.site.editNavLinks) {
            const removeSet = new Set(navHrefsToRemove);
            pano.ui.nav.site.editNavLinks((navItems) =>
              navItems.filter((n) => !removeSet.has(n.href))
            );
            for (const href of navHrefsToRemove) navAddedHrefs.delete(href);
          }

          res.items.forEach((redirect) => {
            // Register dynamic route for each redirect (idempotent: register overwrites
            // by path, so updates to delay/permission/etc. propagate without a stale entry).
            pano.ui.page.register({
              path: redirect.path,
              view: 'redirects:RedirectPage',
              loginRequired: redirect.requireLogin,
              permission: redirect.requirePermission ? redirect.permissionNode : null,
              resetLayout: true, // Always reset layout for redirects as per user request
            });
            registeredPaths.add(redirect.path);

            // Add to Theme Navigation if enabled. Replace any existing entry with the same
            // href so changes to title/target/permission propagate (the old code skipped on
            // existence, leaving stale titles after a rename in the panel).
            if (redirect.showInNavigation && pano.ui.nav.site.editNavLinks) {
              pano.ui.nav.site.editNavLinks((navItems) => {
                const next = navItems.filter((n) => n.href !== redirect.path);
                next.push({
                  href: redirect.path,
                  text: redirect.title, // Literal text
                  target: redirect.openInNewTab ? '_blank' : '_self',
                  startsWith: false,
                  loginRequired: redirect.requireLogin,
                  permission: redirect.requirePermission ? redirect.permissionNode : null,
                });
                return next;
              });
              navAddedHrefs.add(redirect.path);
            }
          });
        } catch (e) {
          console.error('[LinkRedirectsPlugin] Failed to fetch redirects for route registration', e);
        }
      });
    }
  }

  onContextUpdate(ctx) { }

  onUnload() { }
}
