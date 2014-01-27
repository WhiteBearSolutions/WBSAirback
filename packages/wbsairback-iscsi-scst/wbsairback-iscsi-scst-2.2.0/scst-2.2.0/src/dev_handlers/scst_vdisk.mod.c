#include <linux/module.h>
#include <linux/vermagic.h>
#include <linux/compiler.h>

MODULE_INFO(vermagic, VERMAGIC_STRING);

struct module __this_module
__attribute__((section(".gnu.linkonce.this_module"))) = {
 .name = KBUILD_MODNAME,
 .init = init_module,
#ifdef CONFIG_MODULE_UNLOAD
 .exit = cleanup_module,
#endif
 .arch = MODULE_ARCH_INIT,
};

static const struct modversion_info ____versions[]
__used
__attribute__((section("__versions"))) = {
	{ 0x22137d7e, "module_layout" },
	{ 0x3ec8886f, "param_ops_int" },
	{ 0x2d7edeb4, "kmem_cache_create" },
	{ 0x4434f76a, "__scst_register_virtual_dev_driver" },
	{ 0xc3c35a41, "kmem_cache_destroy" },
	{ 0xe37001c8, "scst_unregister_virtual_dev_driver" },
	{ 0x42b05117, "scst_sbc_generic_parse" },
	{ 0x536ee951, "scst_cmd_put" },
	{ 0xfd2bc271, "scst_cmd_get" },
	{ 0x4cbd921, "scst_check_local_events" },
	{ 0xb3fc964a, "scst_tg_get_group_info" },
	{ 0x16305289, "warn_slowpath_null" },
	{ 0xf00ae194, "blkdev_issue_discard" },
	{ 0xb89938ff, "scst_set_sense" },
	{ 0x8ec3568, "scst_impl_alua_configured" },
	{ 0x6a9c5303, "scst_lookup_tg_id" },
	{ 0x7ec9bfbc, "strncpy" },
	{ 0x999e8297, "vfree" },
	{ 0xd754cec9, "vfs_read" },
	{ 0xd6ee688f, "vmalloc" },
	{ 0x2fa5a500, "memcmp" },
	{ 0xfc9b737f, "filemap_write_and_wait_range" },
	{ 0x62ce9188, "scst_set_resp_data_len" },
	{ 0x122bd431, "scst_put_buf_full" },
	{ 0x291d8fa5, "scst_get_buf_full" },
	{ 0xe615c3b0, "vfs_writev" },
	{ 0x9327f5ce, "_raw_spin_lock_irqsave" },
	{ 0x8f64aa4, "_raw_spin_unlock_irqrestore" },
	{ 0x8a5b95f0, "bio_put" },
	{ 0xdd1c65f6, "blk_finish_plug" },
	{ 0x692c48a7, "submit_bio" },
	{ 0x43a0458b, "blk_start_plug" },
	{ 0xaa08c4d9, "bio_add_page" },
	{ 0x565cf864, "bio_kmalloc" },
	{ 0x40011dc9, "bio_get_nr_vecs" },
	{ 0x178ef110, "vfs_readv" },
	{ 0x8c910c18, "default_llseek" },
	{ 0x15a2de69, "scst_add_thr_data" },
	{ 0x337cb9a2, "kmem_cache_free" },
	{ 0x9f96c436, "__scst_find_thr_data" },
	{ 0x386f0094, "scst_set_busy" },
	{ 0x57789f1, "blkdev_issue_flush" },
	{ 0xb6d84793, "scst_del_all_thr_data" },
	{ 0x971f8bb5, "scst_cdrom_generic_parse" },
	{ 0xd52bf1ce, "_raw_spin_lock" },
	{ 0x6eb12e0, "scst_set_cmd_error" },
	{ 0xf781bca8, "scst_register_virtual_device" },
	{ 0xbbbb4d6f, "scst_calc_block_shift" },
	{ 0xaafdc258, "strcasecmp" },
	{ 0x7473d6ee, "scst_get_next_lexem" },
	{ 0xe59c3e5a, "scst_get_next_token_str" },
	{ 0x60ea2d6, "kstrtoull" },
	{ 0x28343bad, "scnprintf" },
	{ 0xe914e41e, "strcpy" },
	{ 0xd7fc2c38, "scst_get_setup_id" },
	{ 0x27000b29, "crc32c" },
	{ 0x6df38750, "kmem_cache_alloc" },
	{ 0x5a34a45c, "__kmalloc" },
	{ 0x12c98a3c, "mutex_lock_interruptible" },
	{ 0x8388e758, "scst_unregister_virtual_device" },
	{ 0xe2d5255a, "strcmp" },
	{ 0xc01cf848, "_raw_read_lock" },
	{ 0x7f658e80, "_raw_write_lock" },
	{ 0x83645c24, "mutex_trylock" },
	{ 0xf9a482f9, "msleep" },
	{ 0x467a654a, "scst_sysfs_work_put" },
	{ 0x88aa937d, "scst_sysfs_work_get" },
	{ 0xf0fdf6cb, "__stack_chk_fail" },
	{ 0x50720c5f, "snprintf" },
	{ 0x779cb7b8, "scst_capacity_data_changed" },
	{ 0x799aca4, "local_bh_enable" },
	{ 0x91715312, "sprintf" },
	{ 0x619e82bd, "kobject_put" },
	{ 0xffbeec1b, "scst_resume_activity" },
	{ 0x60deb672, "scst_dev_del_all_thr_data" },
	{ 0xde2b1c1, "mutex_unlock" },
	{ 0xc499ae1e, "kstrdup" },
	{ 0xb3704504, "mutex_lock" },
	{ 0xc57690b2, "scst_mutex" },
	{ 0x962f18ee, "scst_suspend_activity" },
	{ 0x11089ac7, "_ctype" },
	{ 0x64f78d25, "filp_close" },
	{ 0x9526d1e4, "filp_open" },
	{ 0x37a0cba, "kfree" },
	{ 0x733c3b54, "kasprintf" },
	{ 0x2fb2dcfd, "scst_sysfs_queue_wait_work" },
	{ 0x55a1620d, "kobject_get" },
	{ 0x39bed7a2, "scst_alloc_sysfs_work" },
	{ 0x273d1a8a, "debug_print_prefix" },
	{ 0x27e1a049, "printk" },
	{ 0xb511f740, "current_task" },
	{ 0x4c4fef19, "kernel_stack" },
};

static const char __module_depends[]
__used
__attribute__((section(".modinfo"))) =
"depends=scst,libcrc32c";


MODULE_INFO(srcversion, "C9A700B115BCA6589EB0EAB");
