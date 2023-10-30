#!/bin/bash
#======================================================================
#== Example script to configure a stage specific API Manager using
#== the single "config" command
#======================================================================
set -euo pipefail

CMD_NAME=$(basename $0)
CMD_HOME=$(dirname $0)

helpAndExit() {
  echo "${CMD_NAME}  -  build and configure YAMLES archive"
  echo ""
  echo "Usage:"
  echo "  ${CMD_NAME} -e ENV [--mode MODE] [--debug]"
  echo ""
  echo "Options:"
  echo "  -e ENV"
  echo "      Target environment (local, test or prod)."
  echo ""
  echo "  --mode MODE"
  echo "      Execution mode (CONFIG, DRY_RUN, SYNTAX_CHECK)"
  echo ""
  echo "  --debug"
  echo "      Enable debug messages"
  echo ""  
  echo "  Configure API Manager for target environment and build"
  echo "  deployment archive."
  echo ""
  exit 1
}

parseArgs() {
  DEBUG=0
  MODE="CONFIG"
  while [[ $# -gt 0 ]]
  do
    option="$1"
    case $option in
      -e)
        if [[ $# -gt 1 ]]
        then
          OPT_ENV="$2"
          shift 2
          case $OPT_ENV in
            local)
              ;;
            test)
              ;;
            prod)
              ;;
            *)
              echo "ERROR: unknown environment"
              exit 1
          esac
        else
          echo "ERROR: missing environment"
          exit 1
        fi
        ;;

      --mode)
        if [[ $# -gt 1 ]]
        then
          MODE="$2"
          shift 2
        else
          echo "ERROR: missing mode"
          exit 1
        fi
        ;;

      --debug)
        DEBUG=1
        shift
        ;;

      *)
        echo "ERROR: unknown option $1"
        exit 1
    esac
  done
}

checkArgs() {
  if [ -z "${OPT_ENV:-}" ]
  then
    echo "ERROR: missing -e option"
    exit 1
  fi
}

# Parse command line
if [ $# -lt 1 ]
then
  helpAndExit
fi

parseArgs $*
checkArgs

# Check prerequistes
if [ -z "${AXWAY_HOME:-}" ]
then
  echo "ERROR: AXWAY_HOME not set"
  exit 1
fi
if [ ! -d "${AXWAY_HOME}/apigateway" ]
then
  echo "ERROR: invalid AXWAY_HOME directory: ${AXWAY_HOME}"
  exit 1
fi

# Define variables
PRJ_HOME="$(realpath ${CMD_HOME}/../../..)"
PRJ_SRC="${PRJ_HOME}/examples/apim"

CFG_DIR="$(realpath ${CMD_HOME}/../config)"
RULES_DIR="$(realpath ${CMD_HOME}/../rules)"
LOOKUP_DIR="$(realpath ${CMD_HOME}/../lookup)"

TARGET_DIR="${PRJ_HOME}/target"
TARGET_EXAMPLE_DIR="${TARGET_DIR}/example"
TARGET_TMP_DIR="${TARGET_EXAMPLE_DIR}/tmp"
TARGET_ARCHIVE="${TARGET_EXAMPLE_DIR}/apim.tar.gz"

echo ""
echo "############################################################"
echo "## Build project for ${OPT_ENV}"
echo "############################################################"
echo ""
echo "============================================================"
echo "== Tooling"
echo "============================================================"
# Unzip package
if [ -d "${TARGET_DIR}" ]
then
  YAMLES_UTILS_ZIP=$(find "${TARGET_DIR}" -maxdepth 1 -type f -name "yamles-utils-*.zip")
fi
if [ -z "${YAMLES_UTILS_ZIP:-}" ]
then
  echo "ERROR: yamles-utils archive not found; build project first"
  echo "ERROR:   $ mvn clean package"
  exit 1
fi
if [ ! -d "${TARGET_EXAMPLE_DIR}" ]
then
  mkdir -p "${TARGET_EXAMPLE_DIR}"
fi
YAMLES_UTILS_HOME=$(find "${TARGET_EXAMPLE_DIR}" -mindepth 1 -maxdepth 1 -type d -name "yamles-utils-*")
if [ -z "${YAMLES_UTILS_HOME}" ]
then
  unzip -q -d "${TARGET_EXAMPLE_DIR}" "${YAMLES_UTILS_ZIP}"
  YAMLES_UTILS_HOME=$(find "${TARGET_EXAMPLE_DIR}" -mindepth 1 -maxdepth 1 -type d -name "yamles-utils-*")  
fi

# Set tools
YAMLES_UTILS="${YAMLES_UTILS_HOME}/bin/yamlesutils.sh"
AXWAY_BIN="${AXWAY_HOME}/apigateway/posix/bin"
YAMLES="${AXWAY_BIN}/yamles"
if [ ! -z "${WINDIR:-}" ]
then
  AXWAY_BIN="${AXWAY_HOME}/apigateway/Win32/bin"
  YAMLES="${AXWAY_BIN}/yamles.bat"
  YAMLES_UTILS="${YAMLES_UTILS_HOME}/bin/yamlesutils.cmd"
fi
echo "yamles: ${YAMLES}"
echo "yamlesutils: ${YAMLES_UTILS}"

# Add debug option
if [ ${DEBUG} -gt 0 ]
then
  YAMLES_UTILS="${YAMLES_UTILS} -vv"
fi

# Prepare for configuration
echo ""
echo "============================================================"
echo "== Prepare for configuration"
echo "============================================================"
if [ -d "${TARGET_TMP_DIR}" ]
then
  rm -r "${TARGET_TMP_DIR}"
fi
TMP_PRJ="${TARGET_TMP_DIR}/apim"

mkdir -p "${TMP_PRJ}"
cp -rt "${TMP_PRJ}" "${PRJ_SRC}/."
echo "INFO : temp. project created ${TMP_PRJ}"

echo ""
echo "============================================================"
echo "== Prepare environments variables"
echo "============================================================"
export KDB_PWD_DEV="changeme-devs"
export KDB_PWD_OPS="changeme"

case $OPT_ENV in
  local)
    ;;
  test)
    export DB_METRICS_USER="metrics_t"
    export LOOKUP_JSON_TEST='{"db":{"metrics":{"password": "changeme_t"}}}'
    ;;
  prod)
    export DB_METRICS_USER="metrics_p"
    export LOOKUP_JSON_PROD='{"db":{"metrics":{"password": "changeme_p"}}}'
    ;;
esac


echo ""
echo "============================================================"
echo "== Configure project"
echo "============================================================"
args=("--audit=${TARGET_TMP_DIR}/audit.log" \
      "config" "--mode=${MODE}" "--project=${TMP_PRJ}" \
      "--rules" "${RULES_DIR}/cassandra.rules.yaml" \
      "--rules" "${RULES_DIR}/db.rules.yaml" \
     )

# Lookup Functions
case $OPT_ENV in
  local)
    args+=( \
      # Use local lookups for passphrases
      "--lookup-functions=${CFG_DIR}/devs/local/lookup-func.yaml" \
    )
    ;;
  test)
    args+=( \
      # Use ops-test lookups for secrets
      "--lookup-functions=${CFG_DIR}/ops/test/lookup-func.yaml" \
    )
    ;;
  prod)
    args+=( \
      # Use ops-prod lookups for secrets
      "--lookup-functions=${CFG_DIR}/ops/prod/lookup-func.yaml" \
    )
    ;;
  *)
    echo "ERROR: unsupported environment"
    exit 1
esac

# Certificates
case $OPT_ENV in
  local)
    args+=( \
      # Configure certificates for local development environment
      "--certs=${CFG_DIR}/devs/local/certificates.yaml" \
    )
    ;;
  test)
    args+=( \
      # Configure test certificates provided by operators
      "--certs=${CFG_DIR}/ops/test/certificates.yaml" \
    )
    ;;
  prod)
    args+=( \
      # Configure production certificates provided by operators
      "--certs=${CFG_DIR}/ops/prod/certificates.yaml" \
    )
    ;;
  *)
    echo "ERROR: unsupported environment"
    exit 1
esac

# Fragements
case $OPT_ENV in
  local)
    args+=( \
      # Configure general settings
      "--fragment=${CFG_DIR}/devs/local/values.yaml" \

      # Configure values for which only developers are responsible for
      # in a separate file.
      "--fragment=${CFG_DIR}/devs/local/devs-values.yaml" \
    )
    ;;
  test)
    args+=( \
      # Read values for which only developers are responsible for
      "--fragment=${CFG_DIR}/devs/test/devs-values.yaml" \

      # Configure lookup functions (developers)
      "--lookup-functions=${CFG_DIR}/devs/test/lookup-func.yaml" \

      # Read values for which operators are responsible for
      # (must be after the developers to configuration to prevent overwrite)
      "--fragment=${CFG_DIR}/ops/all/values.yaml" \
      "--fragment=${CFG_DIR}/ops/test/values.yaml" \
    )
    ;;
  prod)
    args+=( \
      # Read values for which only developers are responsible for    
      "--fragment=${CFG_DIR}/devs/prod/devs-values.yaml" \

      # Configure lookup functions (developers)
      "--lookup-functions=${CFG_DIR}/devs/prod/lookup-func.yaml" \

      # Read values for which operators are responsible for
      # (must be after the developers to configuration to prevent overwrite)
      "--fragment=${CFG_DIR}/ops/all/values.yaml" \
      "--fragment=${CFG_DIR}/ops/prod/values.yaml" \
    )
    ;;
  *)
    echo "ERROR: unsupported environment"
    exit 1
esac

if [ ${DEBUG} -gt 0 ]
then
  echo "Arguments: ${args[@]}"
fi
${YAMLES_UTILS} "${args[@]}"

if [ "${MODE}" == "CONFIG" ]
then
  # Build archive
  echo ""
  echo "============================================================"
  echo "== Build archive"
  echo "============================================================"
  rm "${TMP_PRJ}/META-INF/manifest.mf"
  tar -C "${TMP_PRJ}" -zcf ${TARGET_ARCHIVE} "."

  # Validate archive
  echo ""
  echo "============================================================"
  echo "== Validate configured project"
  echo "============================================================"
  ${YAMLES} validate -s "${TARGET_ARCHIVE}"

  echo ""
  echo "============================================================"
  echo "== Finished"
  echo "============================================================"
  echo ""
  echo "Use the following command to deploy the configured archive to"
  echo "the API Gateway."
  echo ""
  echo "  ${AXWAY_BIN}/managedomain --deploy \\"
  echo "    --archive_filename ${TARGET_ARCHIVE} \\"
  echo "    --group APIM \\"
  echo "    --username admin --password changeme"
fi