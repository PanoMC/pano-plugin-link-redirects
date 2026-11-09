<tr>
  <th scope="row" class="align-middle text-center" style="width: 60px;">
    <div class="dropdown position-static">
      <button
        type="button"
        class="btn btn-link focus-ring rounded"
        aria-expanded="false"
        aria-haspopup="true"
        data-bs-toggle="dropdown"
        title={$_('pages.redirects.actions.label')}
        aria-label={$_('pages.redirects.actions.label')}>
        <span class="fas fa-ellipsis-v"></span>
      </button>
      <div class="dropdown-menu dropdown-menu-start">
        <button type="button" class="dropdown-item" on:click={() => onEditClick(redirect.id)}>
          <span>
            <i class="fas fa-edit me-2"></i>
            {$_('common.edit')}
          </span>
        </button>
        <button type="button" class="dropdown-item" on:click={copyLink}>
          <span>
            <i class="fas fa-copy me-2"></i>
            {$_('pages.redirects.actions.copy-link')}
          </span>
        </button>
        <button
          type="button"
          class="dropdown-item link-danger"
          on:click={() => onDeleteClick(redirect.id)}>
          <i class="fas fa-trash me-2"></i>
          <span> {$_('common.delete')} </span>
        </button>
      </div>
    </div>
  </th>
  <td class="align-middle">
    <code>{redirect.id}</code>
  </td>
  <td class="align-middle">
    <div class="text-truncate">
      <button
        type="button"
        title={$_('common.edit')}
        aria-label={$_('common.edit')}
        on:click={() => onEditClick(redirect.id)}
        class="btn btn-link p-0 text-start text-decoration-none w-100 text-truncate">
        {redirect.title}
      </button>
      {#if redirect.useCustomPage}
        <span class="badge text-bg-gray rounded-pill ms-1">{$_('pages.redirects.custom-page')}</span>
      {/if}
    </div>
  </td>
  <td class="align-middle">
    <a
      href={redirect.path}
      target="_blank"
      rel="noopener noreferrer"
      title={$_('common.view')}
      aria-label={$_('common.view')}
      class="text-decoration-none">
      {redirect.path}
    </a>
  </td>
  <td class="align-middle">
    <div class="text-truncate" style="max-width: 250px;">
      <a
        href={/^(https?:\/\/|\/(?![\/\\]))/i.test(redirect.targetUrl || '') ? redirect.targetUrl : undefined}
        target="_blank"
        rel="noopener noreferrer"
        title={$_('common.view')}
        aria-label={$_('common.view')}
        class="text-decoration-none">
        {redirect.targetUrl}
      </a>
    </div>
  </td>
  <td class="align-middle text-nowrap">
    {redirect.delay}{$_('common.seconds-short') || 's'}
  </td>
</tr>

<script>
  import { _, showSuccessToast } from '../../main';
  import {copy} from '@panomc/sdk/utils/text';

  export let redirect;
  export let onEditClick;
  export let onDeleteClick;

  function copyLink() {
    const url = new URL(window.location.origin + redirect.path);
    copy(url.toString());
    showSuccessToast('plugins.pano-plugin-link-redirects.pages.redirects.toasts.copy-success');
  }
</script>
