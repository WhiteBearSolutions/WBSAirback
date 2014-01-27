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
	{ 0xf653034c, "__scst_register_dev_driver" },
	{ 0x42a3b2fb, "scst_modisk_generic_parse" },
	{ 0x4cbd921, "scst_check_local_events" },
	{ 0xa9b843ce, "scst_block_generic_dev_done" },
	{ 0xf0fdf6cb, "__stack_chk_fail" },
	{ 0x33917d1b, "scst_obtain_device_parameters" },
	{ 0x4ac809e7, "debug_print_buffer" },
	{ 0xbbbb4d6f, "scst_calc_block_shift" },
	{ 0x515a2363, "scst_analyze_sense" },
	{ 0xc024383e, "scsi_execute" },
	{ 0x6df38750, "kmem_cache_alloc" },
	{ 0x33a55491, "kmalloc_caches" },
	{ 0x273d1a8a, "debug_print_prefix" },
	{ 0x5a34a45c, "__kmalloc" },
	{ 0x37a0cba, "kfree" },
	{ 0x449b8adc, "scst_unregister_dev_driver" },
	{ 0x27e1a049, "printk" },
	{ 0xb511f740, "current_task" },
};

static const char __module_depends[]
__used
__attribute__((section(".modinfo"))) =
"depends=scst";


MODULE_INFO(srcversion, "148B82F775FBD9CFCF9E431");
