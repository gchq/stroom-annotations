import {
    EDIT_ANNOTATION,
    REQUEST_UPDATE_ANNOTATION,
    RECEIVE_UPDATE_ANNOTATION,
    RECEIVE_UPDATE_ANNOTATION_FAILED
} from '../actions/updateAnnotation'

import {
    REQUEST_CREATE_ANNOTATION,
    RECEIVE_CREATE_ANNOTATION,
    RECEIVE_CREATE_ANNOTATION_FAILED
} from '../actions/createAnnotation'

import {
    REQUEST_FETCH_ANNOTATION,
    RECEIVE_FETCH_ANNOTATION,
    RECEIVE_FETCH_ANNOTATION_NOT_EXIST,
    RECEIVE_FETCH_ANNOTATION_FAILED
} from '../actions/fetchAnnotation'

import {
    REQUEST_REMOVE_ANNOTATION,
    RECEIVE_REMOVE_ANNOTATION,
    RECEIVE_REMOVE_ANNOTATION_FAILED
} from '../actions/removeAnnotation'

const defaultState = {
    isClean: true,
    annotation: {}
}

const singleAnnotation = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case RECEIVE_REMOVE_ANNOTATION:
            return Object.assign({}, state, {
                    isClean: true,
                    lastUpdated: action.receivedAt,
                    annotation: {}
                })

        case RECEIVE_REMOVE_ANNOTATION_FAILED:
            return Object.assign({}, state, {
                    isClean: false,
                    lastUpdated: action.receivedAt,
                    annotation: {}
                  })

        case REQUEST_CREATE_ANNOTATION:
        case REQUEST_FETCH_ANNOTATION:
            return Object.assign({}, state, {
                    isClean: false,
                    annotationId: action.id,
                    annotation: {}
                  })

        case EDIT_ANNOTATION:
            return Object.assign({}, state, {
                    isClean: false,
                    annotation: {
                        ...state.annotation,
                        ...action.updates,
                    }
                  })

        case REQUEST_UPDATE_ANNOTATION:
            return Object.assign({}, state, {
                    isClean: false,
                    annotation: {
                        ...state.annotation,
                        ...action.annotation,
                    }
                  })

        case REQUEST_REMOVE_ANNOTATION:
            return Object.assign({}, state, {
                    isClean: false,
                  })

        case RECEIVE_CREATE_ANNOTATION:
        case RECEIVE_FETCH_ANNOTATION:
            return Object.assign({}, state, {
                    isClean: true,
                    annotation: action.annotation
                  })

        case RECEIVE_FETCH_ANNOTATION_NOT_EXIST:
            return Object.assign({}, state, {
                    isClean: true,
                    annotation: {}
                  })

        case RECEIVE_UPDATE_ANNOTATION:
            return Object.assign({}, state, {
                    isClean: true,
                    annotation: action.annotation
                  })

        case RECEIVE_CREATE_ANNOTATION_FAILED:
            return Object.assign({}, state, {
                    isClean: false
                  })

        case RECEIVE_UPDATE_ANNOTATION_FAILED:
            return Object.assign({}, state, {
                    isClean: false
                  })

        // Of no real concern, it means no annotation exists, so the option to create one will be presented.
        case RECEIVE_FETCH_ANNOTATION_FAILED:
            return Object.assign({}, state, {
                    isClean: true
                  })
        default:
            return state;
    }

}

export default singleAnnotation