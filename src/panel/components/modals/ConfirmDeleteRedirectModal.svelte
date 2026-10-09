<div class="modal fade" bind:this={$modalElement} tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-body text-center">
        <div class="pb-3">
          <i class="fas fa-question-circle fa-3x d-block m-auto text-gray"></i>
        </div>
        <p>
          {$_('pages.redirects.modals.delete-confirm', {
             values: { title: $redirect?.title || '' }
          })}
        </p>
      </div>
      <div class="modal-footer flex-nowrap">
        <button
          type="button"
          class="btn btn-link col-6 m-0"
          on:click={hide}>
          {$_('common.cancel')}
        </button>
        <button
          type="button"
          class="btn btn-danger col-6 m-0"
          on:click={onDelete}
          disabled={loading}>
          {#if loading}
            <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
          {/if}
          {$_('common.delete')}
        </button>
      </div>
    </div>
  </div>
</div>

<script context="module">
    import {get, writable} from 'svelte/store';

    const modalElement = writable();
  const redirect = writable(null);
  let callback = () => {};
  let modal;

  export function show(data) {
    redirect.set(data);
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
  import { api } from '@panomc/sdk/plugin-api';
  import { _, showSuccessToast, showErrorToast } from '../../../main';

  let loading = false;

  async function onDelete() {
    if (!$redirect) return;
    loading = true;

    const res = await api.panel.delete({
      path: `/link-redirects/${$redirect.id}`,
    });

    loading = false;
    if (!res.error) {
      hide();
      callback();
      showSuccessToast('plugins.pano-plugin-link-redirects.pages.redirects.toasts.delete-success');
    } else {
      showErrorToast('common.error');
    }
  }
</script>
