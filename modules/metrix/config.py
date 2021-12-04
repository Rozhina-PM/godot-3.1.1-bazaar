def can_build(env, plat):
	return plat=="android"

def configure(env):
    if (env['platform'] == 'android'):
        env.android_add_java_dir(".")
#        env.android_add_dependency("implementation 'com.google.android.gms:play-services-analytics:16.0.2'")
        env.android_add_dependency("implementation 'com.android.installreferrer:installreferrer:1.0'")
        env.android_add_dependency("implementation 'ir.metrix:metrix:0.14.3'")
        env.android_add_to_manifest("AndroidManifestChunk.xml")
        env.disable_module()

