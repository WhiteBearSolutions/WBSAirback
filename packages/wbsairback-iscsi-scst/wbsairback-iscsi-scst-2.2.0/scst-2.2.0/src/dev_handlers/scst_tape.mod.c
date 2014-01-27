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
	{ 0x6ca615f2, "scst_tape_generic_parse" },
	{ 0x4cbd921, "scst_check_local_events" },
	{ 0x62ce9188, "scst_set_resp_data_len" },
	{ 0x6eb12e0, "scst_set_cmd_error" },
	{ 0x51812cf9, "scst_tape_generic_dev_done" },
	{ 0x33917d1b, "scst_obtain_device_parameters" },
	{ 0x77f1d327, "scsi_mode_sense" },
	{ 0xa3cdb211, "scsi_test_unit_ready" },
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


MODULE_INFO(srcversion, "E6BEC223C9CA1BA36FE1881");
