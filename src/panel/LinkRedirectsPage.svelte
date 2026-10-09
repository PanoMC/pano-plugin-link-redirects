<article class="container vstack gap-3">
  <PageActions leftClasses="d-lg-flex d-none" middleClasses="d-lg-flex d-none">
    <div slot="right">
      <button type="button" class="btn btn-secondary" on:click={onCreateClick}>
        <i class="fas fa-plus"></i>
        <span class="d-lg-inline d-none ms-2"> {$_('pages.redirects.new-redirect')}</span>
      </button>
    </div>
  </PageActions>

  <div class="card">
    <CardHeader>
      <div slot="left">
        {$_('pages.redirects.count', { values: { count: data.totalCount || 0 } })}
      </div>
    </CardHeader>

    {#if !data.redirects || data.redirects.length === 0}
      <NoContent />
    {:else}
      <div class="table-responsive">
        <table class="table table-hover">
          <thead>
            <tr>
              <th scope="col" class="align-middle text-nowrap" style="width: 40px;"></th>
              <th scope="col" class="align-middle text-nowrap" style="width: 60px;">
                {$_('pages.redirects.table.id')}</th>
              <th scope="col" class="align-middle text-nowrap">
                {$_('pages.redirects.table.title')}</th>
              <th scope="col" class="align-middle text-nowrap">
                {$_('pages.redirects.table.path')}</th>
              <th scope="col" class="align-middle text-nowrap">
                {$_('pages.redirects.table.target')}</th>
              <th scope="col" class="align-middle text-nowrap" style="width: 80px;">
                {$_('pages.redirects.table.delay')}</th>
            </tr>
          </thead>
          <tbody>
            {#each data.redirects as redirect (redirect.id)}
              <RedirectRow
                {redirect}
                {onEditClick}
                {onDeleteClick} />
            {/each}
          </tbody>
        </table>
      </div>
      <div class="card-footer">
        <Pagination
          {...paginationProps(data.page, data.pageInfo)}
          on:firstPageClick={() => onPageClick(1)}
          on:lastPageClick={() => onPageClick(data.pageInfo.totalPages)}
          on:pageLinkClick={(event) => onPageClick(event.detail.page)} />
      </div>
    {/if}
  </div>

  <AddEditRedirectModal />
  <ConfirmDeleteRedirectModal />
</article>

<script context="module">
    import { buildQueryParams } from '@panomc/sdk/utils/api';
    import { api } from '@panomc/sdk/plugin-api';
    import { paginationProps } from './pagination.js';

    export async function load(event) {
    const {
      parent,
      url: { searchParams },
    } = event;
    const { pageTitle } = await parent();

    pageTitle.set('plugins.pano-plugin-link-redirects.pages.redirects.title');

    const page = searchParams.get('page') || 1;
    const queryParams = buildQueryParams({ page });

    const body = await api.panel.get({
      path: '/link-redirects' + queryParams,
      request: event,
    });

    if (body.error) {
      return {
        data: {
          redirects: [],
          totalCount: 0,
          pageInfo: { number: 1, size: 10, totalItems: 0, totalPages: 1 },
          page: 1,
        },
      };
    }

    return {
      data: {
        redirects: body.items,
        totalCount: body.page.totalItems,
        pageInfo: body.page,
        page: parseInt(page),
      },
    };
  }
</script>

<script>
  import { base, goto } from '@panomc/sdk/svelte';
  import {
    PageActions,
    CardHeader,
    NoContent,
    Pagination,
  } from '@panomc/sdk/components/panel';
  import { _ } from '../main';
  import RedirectRow from './components/RedirectRow.svelte';
  import AddEditRedirectModal, {
    show as showAddEditModal,
    setCallback as setAddEditCallback,
  } from './components/modals/AddEditRedirectModal.svelte';
  import ConfirmDeleteRedirectModal, {
    show as showDeleteModal,
    setCallback as setDeleteCallback,
  } from './components/modals/ConfirmDeleteRedirectModal.svelte';

  export let data;

  async function refreshData() {
    const pageNum = data.page === 1 ? null : data.page;
    const queryParams = buildQueryParams({ page: pageNum });
    await goto(`${base}/link-redirects${queryParams}`, { invalidateAll: true });
  }

  async function onPageClick(pageNum) {
    data.page = pageNum;
    await refreshData();
  }

  function onCreateClick() {
    showAddEditModal('create');
  }

  function onEditClick(id) {
    const redirect = data.redirects.find((r) => r.id === id);
    if (redirect) {
      showAddEditModal('edit', redirect);
    }
  }

  function onDeleteClick(id) {
    const redirect = data.redirects.find((r) => r.id === id);
    if (redirect) {
      showDeleteModal(redirect);
    }
  }

  setAddEditCallback(() => refreshData());
  setDeleteCallback(() => refreshData());
</script>
