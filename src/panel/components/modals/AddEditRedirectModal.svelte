<div class="modal fade" bind:this={$modalElement} tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">
          {$mode === 'create'
            ? $_('pages.redirects.modals.create-title')
            : $_('pages.redirects.modals.edit-title')}
        </h5>
        <button type="button" class="btn-close" aria-label={$_('common.close')} on:click={hide}
        ></button>
      </div>
      <div class="modal-body">
        <div class="vstack gap-3">
          <div class="row g-3">
            <div class="col-md-6">
              <label for="redirect-title" class="form-label text-capitalize"
                >{$_('pages.redirects.fields.title')}</label>
              <input
                type="text"
                id="redirect-title"
                class="form-control"
                bind:value={$formData.title}
                required />
            </div>
            <div class="col-md-6">
              <label for="redirect-path" class="form-label text-capitalize"
                >{$_('pages.redirects.fields.path')}</label>
              <div class="input-group">
                <span class="input-group-text">/</span>
                <input
                  type="text"
                  id="redirect-path"
                  class="form-control"
                  placeholder={$_('pages.redirects.fields.placeholders.path')}
                  on:input={(e) => {
                    let val = e.target.value;
                    // Remove leading slash if user typed it
                    if (val.startsWith('/')) {
                      val = val.substring(1);
                    }
                    $formData.path = '/' + val;
                  }}
                  value={$formData.path && $formData.path.startsWith('/')
                    ? $formData.path.substring(1)
                    : $formData.path}
                  required />
              </div>
              <div class="form-text">
                {$_('pages.redirects.fields.path-hint')}:
                <a href="{origin}{$formData.path || ''}" target="_blank"
                  >{origin}{$formData.path || ''}</a>
              </div>
            </div>
            <div class="col-12">
              <label for="redirect-target" class="form-label text-capitalize"
                >{$_('pages.redirects.fields.target')}</label>
              <input
                type="url"
                id="redirect-target"
                class="form-control"
                bind:value={$formData.targetUrl}
                placeholder={$_('pages.redirects.fields.placeholders.target')}
                required />
            </div>
          </div>

          <div class="row g-3">
            <div class="col-md-6 vstack gap-2">
              <div class="form-check form-switch">
                <input
                  class="form-check-input"
                  type="checkbox"
                  id="redirect-intermediate"
                  bind:checked={$formData.showIntermediatePage} />
                <label class="form-check-label text-capitalize" for="redirect-intermediate">
                  {$_('pages.redirects.fields.show-intermediate')}
                </label>
              </div>
            </div>

            <div class="col-md-6 vstack gap-2">
              <div class="form-check form-switch">
                <input
                  class="form-check-input"
                  type="checkbox"
                  id="redirect-nav"
                  bind:checked={$formData.showInNavigation} />
                <label class="form-check-label text-capitalize" for="redirect-nav">
                  {$_('pages.redirects.fields.show-in-nav')}
                </label>
              </div>
              <div class="form-check form-switch">
                <input
                  class="form-check-input"
                  type="checkbox"
                  id="redirect-new-tab"
                  disabled={!$formData.showInNavigation}
                  bind:checked={$formData.openInNewTab} />
                <label
                  class="form-check-label text-capitalize"
                  class:text-muted={!$formData.showInNavigation}
                  for="redirect-new-tab">
                  {$_('pages.redirects.fields.open-new-tab')}
                </label>
              </div>
            </div>

            <div class="col-12">
              <div class="row g-2 g-md-3 align-items-end">
                <div class="col-12 col-md-6">
                  <label
                    for="redirect-delay"
                    class="form-label small text-capitalize mb-1"
                    class:text-muted={!$formData.showIntermediatePage}>
                    {$_('pages.redirects.fields.delay')}</label>
                  <div class="input-group input-group-sm">
                    <input
                      type="number"
                      id="redirect-delay"
                      class="form-control"
                      disabled={!$formData.showIntermediatePage}
                      bind:value={$formData.delay}
                      min="0" />
                    <span class="input-group-text">{$_('common.seconds')}</span>
                  </div>
                </div>

                <div class="col-12 col-md-6">
                  <label
                    for="redirect-design"
                    class="form-label small text-capitalize mb-1"
                    class:text-muted={!$formData.showIntermediatePage}>
                    {$_('pages.redirects.fields.design')}</label>
                  <select
                    id="redirect-design"
                    class="form-select form-select-sm"
                    disabled={!$formData.showIntermediatePage}
                    bind:value={$formData.intermediatePageDesign}>
                    <option value="DEFAULT"
                      >{$_('pages.redirects.fields.design-options.default')}</option>
                    <option value="MINIMAL"
                      >{$_('pages.redirects.fields.design-options.minimal')}</option>
                    <option value="MODERN"
                      >{$_('pages.redirects.fields.design-options.modern')}</option>
                    <option value="CUSTOM"
                      >{$_('pages.redirects.fields.design-options.custom') || 'Custom'}</option>
                  </select>
                </div>
              </div>
            </div>

            {#if $formData.showIntermediatePage && $formData.intermediatePageDesign === 'CUSTOM'}
              <div class="col-12 mt-2">
                <label for="redirect-custom-content" class="form-label fw-bold small text-capitalize"
                  >{$_('pages.redirects.fields.custom-content')}</label>
                <Editor
                  id="redirect-custom-content"
                  bind:content={$formData.htmlContent}
                  showHtml={true}
                  showPreview={true}
                  contentStyles="min-height: 300px;" />
              </div>
            {/if}
          </div>

          <hr class="my-2" />

          <div class="row g-3">
            <div class="col-md-6 vstack gap-2">
              <div class="form-check form-switch">
                <input
                  class="form-check-input"
                  type="checkbox"
                  id="redirect-login"
                  bind:checked={$formData.requireLogin} />
                <label class="form-check-label text-capitalize" for="redirect-login">
                  {$_('pages.redirects.fields.require-login')}
                </label>
              </div>
            </div>
            <div class="col-md-6 vstack gap-2">
              <div class="form-check form-switch">
                <input
                  class="form-check-input"
                  type="checkbox"
                  id="redirect-perm-req"
                  bind:checked={$formData.requirePermission} />
                <label class="form-check-label text-capitalize" for="redirect-perm-req">
                  {$_('pages.redirects.fields.require-permission')}
                </label>
              </div>

              {#if $formData.requirePermission}
                <div class="ms-4">
                  <label for="redirect-perm-node" class="form-label small text-capitalize"
                    >{$_('pages.redirects.fields.permission-node')}</label>
                  <input
                    type="text"
                    id="redirect-perm-node"
                    class="form-control form-control-sm"
                    bind:value={$formData.permissionNode}
                    placeholder={$_('pages.redirects.fields.placeholders.permission')} />
                </div>
              {/if}
            </div>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button
          type="button"
          class="btn w-100 {$mode === 'create' ? 'btn-secondary' : 'btn-primary'}"
          on:click={onSave}
          disabled={loading || !isValid || !isChanged}>
          {#if loading}
            <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"
            ></span>
          {/if}
          {$mode === 'create' ? $_('common.create') : $_('common.save')}
        </button>
      </div>
    </div>
  </div>
</div>

<script context="module">
  import {get, writable} from 'svelte/store';

  const modalElement = writable();
  const mode = writable('create');
  const formData = writable({});
  const initialFormData = writable({});
  let callback = () => {};
  let modal;

  export function show(newMode, data = {}) {
    mode.set(newMode);

    // Default values
    const defaults = {
      title: '',
      path: '',
      targetUrl: '',
      delay: 0,
      showIntermediatePage: false,
      intermediatePageDesign: 'DEFAULT',
      useCustomPage: false,
      openInNewTab: false,
      showInNavigation: false,
      requireLogin: false,
      requirePermission: false,
      permissionNode: '',
      htmlContent: '',
    };

    // Merge defaults with existing data if present (for edit mode)
    // Replace null values from data with defaults
    const cleanedData = {};
    Object.keys(defaults).forEach((key) => {
      cleanedData[key] = data[key] !== null && data[key] !== undefined ? data[key] : defaults[key];
    });

    // Also include 'id' if present (needed for updates)
    if (data.id) cleanedData.id = data.id;

    const mergedData = cleanedData;

    // Deep copy to break references
    formData.set(JSON.parse(JSON.stringify(mergedData)));
    initialFormData.set(JSON.parse(JSON.stringify(mergedData)));

    modal = new window.bootstrap.Modal(get(modalElement), {
      backdrop: 'static',
      keyboard: false,
    });
    modal.show();
  }

  export function hide() {
    modal.hide();
  }

  export function setCallback(cb) {
    callback = cb;
  }
</script>

<script>
  import { onMount } from 'svelte';
  import { api } from '@panomc/sdk/plugin-api';
  import { Editor } from '@panomc/sdk/components/panel';
  import { _, showSuccessToast, showErrorToast } from '../../../main';

  let loading = false;
  let origin = '';

  onMount(() => {
    origin = window.location.origin;
  });

  $: isValid = (() => {
    if (!$formData.title || $formData.title.trim() === '') return false;
    if (!$formData.path || $formData.path === '/' || $formData.path.trim() === '') return false;
    if (!$formData.targetUrl || $formData.targetUrl.trim() === '') return false;
    if (
      $formData.requirePermission &&
      (!$formData.permissionNode || $formData.permissionNode.trim() === '')
    )
      return false;
    return true;
  })();

  $: isChanged =
    $mode === 'create' || JSON.stringify($formData) !== JSON.stringify($initialFormData);

  $: if (!$formData.showIntermediatePage) {
    if ($formData.delay !== 0 || $formData.useCustomPage !== false) {
      $formData = { ...$formData, delay: 0, useCustomPage: false };
    }
  } else {
    const isCustom = $formData.intermediatePageDesign === 'CUSTOM';
    if ($formData.useCustomPage !== isCustom) {
      $formData = { ...$formData, useCustomPage: isCustom };
    }
  }

  $: if (!$formData.showInNavigation && $formData.openInNewTab) {
    $formData = { ...$formData, openInNewTab: false };
  }

  async function onSave() {
    if (!isValid) return;

    loading = true;
    let res;

    try {
      if ($mode === 'create') {
        res = await api.panel.post({
          path: '/link-redirects',
          body: $formData,
        });
      } else {
        res = await api.panel.put({
          path: `/link-redirects/${$formData.id}`,
          body: $formData,
        });
      }
    } catch (e) {
      console.error(e);
      res = { error: 'UNKNOWN_ERROR' };
    }

    loading = false;
    if (res && !res.error) {
      hide();
      callback();
      showSuccessToast(
        `plugins.pano-plugin-link-redirects.pages.redirects.toasts.${$mode}-success`,
      );
    } else {
      showErrorToast(`common.error`);
    }
  }
</script>
