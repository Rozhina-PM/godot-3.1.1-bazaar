#include "register_types.h"
#include "gdga.h"
#include "../../core/engine.h"
#include "../../core/class_db.h"

Gdga* gdga_instance = 0;

void register_gdga_types()
{
    gdga_instance = memnew(Gdga);
    
    ClassDB::register_class<Gdga>();
    
    Engine::get_singleton()->add_singleton(Engine::Singleton("Gdga", Gdga::get_singleton()));
}

void unregister_gdga_types()
{
}
