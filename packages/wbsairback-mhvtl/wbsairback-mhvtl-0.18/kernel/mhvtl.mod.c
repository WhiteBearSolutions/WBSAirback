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
	{ 0xde2b1c1, "mutex_unlock" },
	{ 0x71e3cecb, "up" },
	{ 0xf9c23888, "scsi_device_put" },
	{ 0x37b247af, "scsi_remove_device" },
	{ 0x68aca4ad, "down" },
	{ 0xb2fd5ceb, "__put_user_4" },
	{ 0xb3704504, "mutex_lock" },
	{ 0xf5999af2, "scsi_cmd_get_serial" },
	{ 0xd9605d4c, "add_timer" },
	{ 0x7d11c268, "jiffies" },
	{ 0x132a7a5b, "init_timer_key" },
	{ 0xc884b5fb, "dev_set_name" },
	{ 0x8cf8eefe, "driver_create_file" },
	{ 0x72c5e90c, "driver_register" },
	{ 0xf43b51eb, "bus_register" },
	{ 0xd5950f0, "device_register" },
	{ 0xdc2fe0b7, "__register_chrdev" },
	{ 0xf366037a, "sg_miter_stop" },
	{ 0x631724aa, "sg_miter_next" },
	{ 0x71de9b3f, "_copy_to_user" },
	{ 0xd9dbdd51, "sg_miter_start" },
	{ 0x77e2f33, "_copy_from_user" },
	{ 0x6bc3fbc0, "__unregister_chrdev" },
	{ 0x74a89692, "bus_unregister" },
	{ 0xaeda5a35, "driver_unregister" },
	{ 0xfc94c010, "device_unregister" },
	{ 0x9a90c3f7, "driver_remove_file" },
	{ 0xcd92672f, "scsi_scan_host" },
	{ 0xb10f2fb3, "scsi_add_host_with_dma" },
	{ 0x9aabd8a7, "scsi_host_alloc" },
	{ 0x91715312, "sprintf" },
	{ 0x71f6eb38, "sg_copy_from_buffer" },
	{ 0x8f64aa4, "_raw_spin_unlock_irqrestore" },
	{ 0x6f0036d9, "del_timer_sync" },
	{ 0x9327f5ce, "_raw_spin_lock_irqsave" },
	{ 0xfe41ef26, "scsi_adjust_queue_depth" },
	{ 0xb39709f8, "scsi_host_put" },
	{ 0xe5b59147, "scsi_remove_host" },
	{ 0x37a0cba, "kfree" },
	{ 0x756e6992, "strnicmp" },
	{ 0x28343bad, "scnprintf" },
	{ 0xf0fdf6cb, "__stack_chk_fail" },
	{ 0x9cdbbf48, "__scsi_add_device" },
	{ 0x6df38750, "kmem_cache_alloc" },
	{ 0x33a55491, "kmalloc_caches" },
	{ 0x85abc85f, "strncmp" },
	{ 0xd52bf1ce, "_raw_spin_lock" },
	{ 0x42224298, "sscanf" },
	{ 0x27e1a049, "printk" },
};

static const char __module_depends[]
__used
__attribute__((section(".modinfo"))) =
"depends=";


MODULE_INFO(srcversion, "B1DB4113F9C0DE327024662");
