def can_build(env, plat):
	return plat=="android"

def configure(env):
    if (env['platform'] == 'android'):
        env.android_add_java_dir(".")
        env.android_add_maven_repository("url 'https://dl.bintray.com/metrixorg/maven'")
        env.android_add_dependency("implementation 'ir.metrix:metrix:0.14.3'")
        env.android_add_dependency("implementation 'com.android.support:support-v4:26.1.0'")
        env.android_add_to_attributes("AttributesChunk.xml")
        env.android_add_to_manifest("AndroidManifestChunk.xml")
        env.android_add_res_dir("res")
        env.disable_module()

