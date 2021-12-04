#
# Generated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add customized code.
#
# This makefile implements configuration specific macros and targets.


# Environment
MKDIR=mkdir
CP=cp
GREP=grep
NM=nm
CCADMIN=CCadmin
RANLIB=ranlib
CC=gcc
CCC=g++
CXX=g++
FC=gfortran
AS=as

# Macros
CND_PLATFORM=GNU-Linux
CND_DLIB_EXT=so
CND_CONF=Release
CND_DISTDIR=dist
CND_BUILDDIR=build

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=${CND_BUILDDIR}/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/_ext/5c0/crash.o \
	${OBJECTDIR}/_ext/5c0/gdga.o \
	${OBJECTDIR}/_ext/5c0/gzip.o \
	${OBJECTDIR}/_ext/5c0/hmac.o \
	${OBJECTDIR}/_ext/5c0/register_types.o \
	${OBJECTDIR}/_ext/5c0/uuid.o


# C Compiler Flags
CFLAGS=

# CC Compiler Flags
CCFLAGS=
CXXFLAGS=

# Fortran Compiler Flags
FFLAGS=

# Assembler Flags
ASFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/gdga

${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/gdga: ${OBJECTFILES}
	${MKDIR} -p ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}
	${LINK.cc} -o ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/gdga ${OBJECTFILES} ${LDLIBSOPTIONS}

${OBJECTDIR}/_ext/5c0/crash.o: ../crash.cpp
	${MKDIR} -p ${OBJECTDIR}/_ext/5c0
	${RM} "$@.d"
	$(COMPILE.cc) -I../../../core -I../../../thirdparty/misc -include ../../../core -include ../../../thirdparty/misc -std=c++11 -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/_ext/5c0/crash.o ../crash.cpp

${OBJECTDIR}/_ext/5c0/gdga.o: ../gdga.cpp
	${MKDIR} -p ${OBJECTDIR}/_ext/5c0
	${RM} "$@.d"
	$(COMPILE.cc) -I../../../core -I../../../thirdparty/misc -include ../../../core -include ../../../thirdparty/misc -std=c++11 -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/_ext/5c0/gdga.o ../gdga.cpp

${OBJECTDIR}/_ext/5c0/gzip.o: ../gzip.cpp
	${MKDIR} -p ${OBJECTDIR}/_ext/5c0
	${RM} "$@.d"
	$(COMPILE.cc) -I../../../core -I../../../thirdparty/misc -include ../../../core -include ../../../thirdparty/misc -std=c++11 -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/_ext/5c0/gzip.o ../gzip.cpp

${OBJECTDIR}/_ext/5c0/hmac.o: ../hmac.cpp
	${MKDIR} -p ${OBJECTDIR}/_ext/5c0
	${RM} "$@.d"
	$(COMPILE.cc) -I../../../core -I../../../thirdparty/misc -include ../../../core -include ../../../thirdparty/misc -std=c++11 -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/_ext/5c0/hmac.o ../hmac.cpp

${OBJECTDIR}/_ext/5c0/register_types.o: ../register_types.cpp
	${MKDIR} -p ${OBJECTDIR}/_ext/5c0
	${RM} "$@.d"
	$(COMPILE.cc) -I../../../core -I../../../thirdparty/misc -include ../../../core -include ../../../thirdparty/misc -std=c++11 -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/_ext/5c0/register_types.o ../register_types.cpp

${OBJECTDIR}/_ext/5c0/uuid.o: ../uuid.cpp
	${MKDIR} -p ${OBJECTDIR}/_ext/5c0
	${RM} "$@.d"
	$(COMPILE.cc) -I../../../core -I../../../thirdparty/misc -include ../../../core -include ../../../thirdparty/misc -std=c++11 -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/_ext/5c0/uuid.o ../uuid.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
