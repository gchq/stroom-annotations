import {
    SET_ALLOW_NAVIGATION,
    SET_ANNOTATION_ID,
    SET_INDEX_UUID
} from '../actions/setupUi'

const defaultState = {
    allowNavigation: true,
    annotationId: undefined,
    indexUuid: undefined
}

const ui = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case SET_ALLOW_NAVIGATION:
            if (!state.allowNavigationAlreadySet) {
                return {
                    ...state,
                    allowNavigation: action.allowNavigation
                }
            } else {
                return state
            }
        case SET_ANNOTATION_ID:
            return {
                ...state,
                annotationId: action.annotationId
            }
        case SET_INDEX_UUID:
            return {
                ...state,
                indexUuid: action.indexUuid
            }
        default:
            return state
    }
}

export default ui
