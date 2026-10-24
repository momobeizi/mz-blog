(function () {
  let toastTimer;
  const showToast = (message) => {
    let toast = document.getElementById('siteToast');
    if (!toast) {
      toast = document.createElement('div');
      toast.id = 'siteToast';
      toast.className = 'site-toast';
      document.body.appendChild(toast);
    }
    toast.textContent = message;
    toast.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove('show'), 2600);
  };

  const header = document.querySelector('.site-header');
  const nav = document.getElementById('siteNav');
  const toggle = document.getElementById('navToggle');
  const onScroll = () => {
    if (!header) return;
    header.classList.toggle('scrolled', window.scrollY > 42);
  };
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();
  toggle?.addEventListener('click', () => nav?.classList.toggle('open'));

  const content = document.getElementById('articleContent');
  const toc = document.getElementById('articleToc');
  if (content && toc) {
    const headings = Array.from(content.querySelectorAll('h2, h3'));
    if (headings.length) {
      toc.innerHTML = '';
      headings.forEach((heading, index) => {
        if (!heading.id) heading.id = 'heading-' + index;
        const link = document.createElement('a');
        link.href = '#' + heading.id;
        link.textContent = heading.textContent;
        if (heading.tagName === 'H3') link.classList.add('toc-h3');
        toc.appendChild(link);
      });
      const links = Array.from(toc.querySelectorAll('a'));
      const observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
          if (!entry.isIntersecting) return;
          links.forEach(link => link.classList.toggle('active', link.getAttribute('href') === '#' + entry.target.id));
        });
      }, { rootMargin: '-90px 0px -70% 0px' });
      headings.forEach(heading => observer.observe(heading));
    }
  }

  const likeButton = document.querySelector('[data-like-article]');
  if (likeButton) {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    likeButton.addEventListener('click', async () => {
      if (likeButton.dataset.loading === 'true') return;
      likeButton.dataset.loading = 'true';
      likeButton.disabled = true;
      try {
        const response = await fetch(`/api/public/articles/${encodeURIComponent(likeButton.dataset.likeArticle)}/like`, {
          method: 'POST',
          credentials: 'same-origin',
          headers: csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {},
        });
        const payload = await response.json().catch(() => null);
        if (!response.ok || !payload || payload.code !== 0) {
          throw new Error(payload?.message || '点赞失败，请稍后再试');
        }
        likeButton.querySelector('.like-count').textContent = payload.data.likeCount;
        likeButton.classList.toggle('liked', payload.data.liked);
        likeButton.querySelector('.like-label').textContent = payload.data.liked ? '已喜欢' : '喜欢';
        showToast(payload.data.message || '感谢你的喜欢');
      } catch (error) {
        showToast(error instanceof Error ? error.message : '点赞失败，请稍后再试');
      } finally {
        likeButton.disabled = false;
        likeButton.dataset.loading = 'false';
      }
    });
  }
})();
