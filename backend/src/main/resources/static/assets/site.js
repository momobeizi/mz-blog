(function () {
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
})();
