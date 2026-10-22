package com.mz.blog.portal;

import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.content.article.ArticleMapper;
import com.mz.blog.content.article.CmsArticle;
import com.mz.blog.system.user.SysUser;
import com.mz.blog.system.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class AdminPreviewController {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    @GetMapping("/admin/preview/articles/{id}")
    @PreAuthorize("hasAuthority('content:article:list')")
    public String articlePreview(@PathVariable Long id, Model model) {
        CmsArticle article = articleMapper.selectById(id);
        if (article == null) throw BusinessException.notFound("文章不存在");
        SysUser author = userMapper.selectById(article.getAuthorId());
        model.addAttribute("article", article);
        model.addAttribute("author", author);
        return "portal/admin-preview";
    }
}
