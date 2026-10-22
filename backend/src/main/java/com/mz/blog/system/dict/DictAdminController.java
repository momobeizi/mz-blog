package com.mz.blog.system.dict;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mz.blog.common.api.ApiResponse;
import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.common.util.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/system/dicts")
@RequiredArgsConstructor
public class DictAdminController {

    private final DictTypeMapper dictTypeMapper;
    private final DictItemMapper dictItemMapper;

    public record TypePayload(@NotBlank String dictCode, @NotBlank String name, Integer status, String remark) {
    }

    public record ItemPayload(Long dictTypeId, @NotBlank String label, @NotBlank String value,
                              Integer sort, Integer status, String remark) {
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:dict:list')")
    public ApiResponse<List<SysDictType>> types(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(dictTypeMapper.selectList(Wrappers.<SysDictType>lambdaQuery()
                .and(keyword != null && !keyword.isBlank(), q -> q.like(SysDictType::getName, keyword)
                        .or().like(SysDictType::getDictCode, keyword))
                .orderByAsc(SysDictType::getId)));
    }

    @GetMapping("/options")
    public ApiResponse<Map<String, List<SysDictItem>>> options() {
        Map<String, List<SysDictItem>> result = new LinkedHashMap<>();
        List<SysDictType> types = dictTypeMapper.selectList(Wrappers.<SysDictType>lambdaQuery()
                .eq(SysDictType::getStatus, 1).orderByAsc(SysDictType::getId));
        for (SysDictType type : types) {
            result.put(type.getDictCode(), dictItemMapper.selectList(Wrappers.<SysDictItem>lambdaQuery()
                    .eq(SysDictItem::getDictTypeId, type.getId())
                    .eq(SysDictItem::getStatus, 1)
                    .orderByAsc(SysDictItem::getSort)));
        }
        return ApiResponse.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:dict:create')")
    public ApiResponse<Void> createType(@Valid @RequestBody TypePayload payload) {
        if (dictTypeMapper.selectCount(Wrappers.<SysDictType>lambdaQuery()
                .eq(SysDictType::getDictCode, payload.dictCode())) > 0) {
            throw BusinessException.conflict("字典编码已存在");
        }
        SysDictType type = new SysDictType();
        type.setDictCode(payload.dictCode().trim());
        type.setName(payload.name().trim());
        type.setStatus(payload.status() == null ? 1 : payload.status());
        type.setRemark(payload.remark());
        type.setCreatedBy(SecurityUtils.currentUserId());
        dictTypeMapper.insert(type);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dict:update')")
    public ApiResponse<Void> updateType(@PathVariable Long id, @Valid @RequestBody TypePayload payload) {
        SysDictType type = requireType(id);
        if (dictTypeMapper.selectCount(Wrappers.<SysDictType>lambdaQuery()
                .eq(SysDictType::getDictCode, payload.dictCode()).ne(SysDictType::getId, id)) > 0) {
            throw BusinessException.conflict("字典编码已存在");
        }
        type.setDictCode(payload.dictCode().trim());
        type.setName(payload.name().trim());
        type.setStatus(payload.status());
        type.setRemark(payload.remark());
        type.setUpdatedBy(SecurityUtils.currentUserId());
        dictTypeMapper.updateById(type);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @Transactional
    public ApiResponse<Void> deleteType(@PathVariable Long id) {
        requireType(id);
        dictItemMapper.delete(Wrappers.<SysDictItem>lambdaQuery().eq(SysDictItem::getDictTypeId, id));
        dictTypeMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("hasAuthority('system:dict:list')")
    public ApiResponse<List<SysDictItem>> items(@PathVariable Long id) {
        requireType(id);
        return ApiResponse.ok(dictItemMapper.selectList(Wrappers.<SysDictItem>lambdaQuery()
                .eq(SysDictItem::getDictTypeId, id)
                .orderByAsc(SysDictItem::getSort).orderByAsc(SysDictItem::getId)));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('system:dict:create')")
    public ApiResponse<Void> createItem(@PathVariable Long id, @Valid @RequestBody ItemPayload payload) {
        requireType(id);
        validateItem(id, payload.value(), null);
        SysDictItem item = new SysDictItem();
        item.setDictTypeId(id);
        applyItem(item, payload);
        item.setCreatedBy(SecurityUtils.currentUserId());
        dictItemMapper.insert(item);
        return ApiResponse.ok();
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAuthority('system:dict:update')")
    public ApiResponse<Void> updateItem(@PathVariable Long itemId, @Valid @RequestBody ItemPayload payload) {
        SysDictItem item = dictItemMapper.selectById(itemId);
        if (item == null) {
            throw BusinessException.notFound("字典项不存在");
        }
        validateItem(item.getDictTypeId(), payload.value(), itemId);
        applyItem(item, payload);
        item.setUpdatedBy(SecurityUtils.currentUserId());
        dictItemMapper.updateById(item);
        return ApiResponse.ok();
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    public ApiResponse<Void> deleteItem(@PathVariable Long itemId) {
        if (dictItemMapper.selectById(itemId) == null) {
            throw BusinessException.notFound("字典项不存在");
        }
        dictItemMapper.deleteById(itemId);
        return ApiResponse.ok();
    }

    private SysDictType requireType(Long id) {
        SysDictType type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw BusinessException.notFound("字典类型不存在");
        }
        return type;
    }

    private void validateItem(Long typeId, String value, Long itemId) {
        if (dictItemMapper.selectCount(Wrappers.<SysDictItem>lambdaQuery()
                .eq(SysDictItem::getDictTypeId, typeId)
                .eq(SysDictItem::getValue, value)
                .ne(itemId != null, SysDictItem::getId, itemId)) > 0) {
            throw BusinessException.conflict("字典值已存在");
        }
    }

    private void applyItem(SysDictItem item, ItemPayload payload) {
        item.setLabel(payload.label().trim());
        item.setValue(payload.value().trim());
        item.setSort(payload.sort() == null ? 0 : payload.sort());
        item.setStatus(payload.status() == null ? 1 : payload.status());
        item.setRemark(payload.remark());
    }
}
